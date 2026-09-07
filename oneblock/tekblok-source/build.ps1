$ErrorActionPreference = "Stop"

Write-Host "[1/4] Temizleniyor..." -ForegroundColor Cyan
if (Test-Path "target\classes") {
    Remove-Item -Recurse -Force "target\classes"
}
New-Item -ItemType Directory -Force -Path "target\classes" | Out-Null

Write-Host "[2/4] Java kaynak kodlari derleniyor (Spigot 1.16.1 API)..." -ForegroundColor Cyan
$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac --release 8 -encoding UTF-8 -cp "libs\spigot-api-1.16.1.jar" -d "target\classes" $javaFiles
if ($LASTEXITCODE -ne 0) {
    Write-Host "[HATA] Java derleme basarisiz oldu!" -ForegroundColor Red
    exit 1
}

Write-Host "[3/4] Kaynak dosyalari (plugin.yml, config.yml vb.) kopyalaniyor..." -ForegroundColor Cyan
Copy-Item -Path "src\main\resources\*" -Destination "target\classes\" -Recurse -Force

Write-Host "[4/4] TekBlok-1.0.0.jar paketleniyor..." -ForegroundColor Cyan
Set-Location "target\classes"
jar -cvf "..\TekBlok-1.0.0.jar" *
Set-Location "..\.."

if (Test-Path "target\TekBlok-1.0.0.jar") {
    $size = (Get-Item "target\TekBlok-1.0.0.jar").Length
    Write-Host "[BASARILI] TekBlok-1.0.0.jar olusturuldu! Boyut: $size bayt" -ForegroundColor Green
    Copy-Item -Path "target\TekBlok-1.0.0.jar" -Destination "TekBlok-1.0.0.jar" -Force
    Write-Host "Kullanima hazir dosya ana dizine kopyalandi: TekBlok-1.0.0.jar" -ForegroundColor Green

    # Eger Desktop\oneblock\plugins sunucu klasoru varsa oraya da otomatik guncelle
    $serverPluginDir = "..\oneblock\plugins"
    if (Test-Path $serverPluginDir) {
        Copy-Item -Path "target\TekBlok-1.0.0.jar" -Destination "$serverPluginDir\TekBlok-1.0.0.jar" -Force
        Write-Host "Sunucu eklenti klasorune otomatik kopyalandi: $serverPluginDir\TekBlok-1.0.0.jar" -ForegroundColor Cyan

        $serverTekBlokDataDir = "$serverPluginDir\TekBlok"
        if (Test-Path $serverTekBlokDataDir) {
            Get-ChildItem -Path "src\main\resources\*.yml" | ForEach-Object {
                $targetFile = Join-Path $serverTekBlokDataDir $_.Name
                # config.yml sunucuda zaten varsa ustune yazma (setspawn ve ozel ayarlar kaybolmasin)
                if ($_.Name -eq "config.yml" -and (Test-Path $targetFile)) {
                    # config.yml korunur
                } else {
                    Copy-Item -Path $_.FullName -Destination $targetFile -Force
                }
            }
            Write-Host "Sunucu TekBlok yapilandirmalari senkronize edildi (config.yml korundu)!" -ForegroundColor Cyan
        }
    }
} else {
    Write-Host "[HATA] Jar dosyasi olusturulamadi!" -ForegroundColor Red
}
