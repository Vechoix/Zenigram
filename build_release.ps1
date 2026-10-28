$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "F:\jdk-21\jdk-21.0.12.1+1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME = "H:\Android\Sdk"
$env:ANDROID_SDK_ROOT = "H:\Android\Sdk"
$env:GRADLE_USER_HOME = "H:\GradleCache"
$env:TEMP = "F:\Temp"
$env:TMP = "F:\Temp"

Write-Host "Compiling Zenigram arm64-v8a Release APK (v1.0.8)..."
& .\gradlew.bat :TMessagesProj_App:assembleRelease -x lint -x test --daemon

if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle release build failed with exit code $LASTEXITCODE"
    exit 1
}

$apkDir = "F:\Nekogram-main\TMessagesProj_App\build\outputs\apk\release"
if (-not (Test-Path $apkDir)) {
    $apkDir = "F:\Nekogram-main\TMessagesProj_App\build\outputs\apk"
}

Write-Host "Locating output release APK in $apkDir..."
$releaseApk = Get-ChildItem -Path $apkDir -Recurse -Filter "*.apk" | Where-Object { $_.FullName -notmatch "\\debug\\" -and ($_.Name -like "*arm64*" -or $_.Name -like "*release*") } | Select-Object -First 1

if (-not $releaseApk) {
    Write-Error "Could not find release APK in $apkDir"
    exit 1
}

$destDir = "F:\Nekogram-main\APKs"
if (-not (Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
}

$targetName = "Zenigram-1.0.8-arm64-v8a.apk"
$destPath = Join-Path $destDir $targetName
Copy-Item -Path $releaseApk.FullName -Destination $destPath -Force

$keystore = "F:\Nekogram-main\zenigram-release.jks"
$apksigner = "H:\Android\Sdk\build-tools\35.0.0\apksigner.bat"
if (Test-Path $keystore) {
    Write-Host "Signing APK with zenigram-release.jks (v1 + v2 schemes)..."
    & $apksigner sign --ks $keystore --ks-key-alias zenigram --ks-pass pass:$env:KEYSTORE_PASS --key-pass pass:$env:KEYSTORE_PASS $destPath
    & $apksigner verify --verbose $destPath
}

$hash = (Get-FileHash -Path $destPath -Algorithm SHA256).Hash
$sizeMb = [math]::Round((Get-Item $destPath).Length / 1MB, 2)
$bytes = (Get-Item $destPath).Length

Write-Host "=========================================="
Write-Host "SUCCESSFULLY BUILT & SIGNED RELEASE APK:"
Write-Host "File:   $destPath"
Write-Host "Size:   $sizeMb MB ($bytes bytes)"
Write-Host "SHA256: $hash"
Write-Host "=========================================="
