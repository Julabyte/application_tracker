$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$appName = "BewerbungsTracker"
$appVersion = "0.1.0"
$mainClass = "com.lemontree.applicationtracker.ApplicationTrackerApp"
$artifactName = "application-tracker-0.1.0-SNAPSHOT.jar"

$target = Join-Path $root "target"
$inputDir = Join-Path $target "package-input"
$installerDir = Join-Path $target "installer"
$imageDir = Join-Path $target "app-image"

function Invoke-Native {
    param(
        [Parameter(Mandatory = $true)]
        [string] $FilePath,
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]] $Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Befehl fehlgeschlagen ($LASTEXITCODE): $FilePath $($Arguments -join ' ')"
    }
}

function Find-JPackage {
    $direct = Get-Command jpackage -ErrorAction SilentlyContinue
    if ($direct) {
        return $direct.Source
    }

    if ($env:JAVA_HOME) {
        $fromJavaHome = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
        if (Test-Path $fromJavaHome) {
            return $fromJavaHome
        }
    }

    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $fromJavaCommand = Join-Path (Split-Path -Parent $javaCommand.Source) "jpackage.exe"
        if (Test-Path $fromJavaCommand) {
            return $fromJavaCommand
        }
    }

    $fromProgramFiles = Get-ChildItem "C:\Program Files\Java" -Recurse -Filter jpackage.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if ($fromProgramFiles) {
        return $fromProgramFiles
    }

    throw "jpackage.exe wurde nicht gefunden. Installiere ein JDK 17+ und setze JAVA_HOME auf den JDK-Ordner."
}

$jpackage = Find-JPackage

Write-Host "Baue Maven-Projekt..."
Push-Location $root
try {
    Invoke-Native "mvn" "-q" "clean" "package" "org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies" "-DincludeScope=runtime" "-DoutputDirectory=$inputDir"
} finally {
    Pop-Location
}

New-Item -ItemType Directory -Force -Path $inputDir | Out-Null
New-Item -ItemType Directory -Force -Path $installerDir | Out-Null

$sourceJar = Join-Path $target $artifactName
$mainJar = Join-Path $inputDir "application-tracker.jar"
Copy-Item -Force -Path $sourceJar -Destination $mainJar

Write-Host "Erzeuge Windows-Installer..."
try {
    Invoke-Native $jpackage `
        "--type" "exe" `
        "--name" $appName `
        "--app-version" $appVersion `
        "--vendor" "LemonTree" `
        "--input" $inputDir `
        "--main-jar" "application-tracker.jar" `
        "--main-class" $mainClass `
        "--dest" $installerDir `
        "--java-options" "--module-path=`$APPDIR --add-modules=javafx.controls,javafx.graphics" `
        "--win-menu" `
        "--win-shortcut"

    Write-Host "Installer erzeugt in: $installerDir"
} catch {
    Write-Warning "EXE-Installer konnte nicht erzeugt werden. Auf Windows braucht jpackage dafuer oft WiX Toolset."
    Write-Warning $_.Exception.Message

    Write-Host "Erzeuge stattdessen portable App..."
    New-Item -ItemType Directory -Force -Path $imageDir | Out-Null
    Invoke-Native $jpackage `
        "--type" "app-image" `
        "--name" $appName `
        "--app-version" $appVersion `
        "--vendor" "LemonTree" `
        "--input" $inputDir `
        "--main-jar" "application-tracker.jar" `
        "--main-class" $mainClass `
        "--dest" $imageDir `
        "--java-options" "--module-path=`$APPDIR --add-modules=javafx.controls,javafx.graphics"

    Write-Host "Portable App erzeugt in: $imageDir"
}
