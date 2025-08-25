# SendGrid Test Script
# This script helps test SendGrid email functionality

param(
    [string]$ApiKey = "",
    [string]$FromEmail = "nexsplit.mail@gmail.com",
    [string]$ToEmail = "test@example.com"
)

Write-Host "=== SendGrid Email Test ===" -ForegroundColor Green

if ([string]::IsNullOrEmpty($ApiKey)) {
    Write-Host "❌ Please provide your SendGrid API key" -ForegroundColor Red
    Write-Host "Usage: .\test-sendgrid.ps1 -ApiKey 'SG.your_api_key_here' -ToEmail 'your-email@example.com'" -ForegroundColor Yellow
    exit 1
}

Write-Host "API Key: $($ApiKey.Substring(0, 10))..." -ForegroundColor Cyan
Write-Host "From Email: $FromEmail" -ForegroundColor Cyan
Write-Host "To Email: $ToEmail" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check API Key Format
if (-not $ApiKey.StartsWith("SG.")) {
    Write-Host "❌ Invalid API key format. Should start with 'SG.'" -ForegroundColor Red
    exit 1
}

Write-Host "✅ API key format is valid" -ForegroundColor Green

# Test 2: Test Network Connectivity
Write-Host "Testing network connectivity to SendGrid..." -ForegroundColor Yellow
try {
    $result = Test-NetConnection -ComputerName "smtp.sendgrid.net" -Port 587 -InformationLevel Quiet
    if ($result) {
        Write-Host "✅ Network connectivity to SendGrid is working" -ForegroundColor Green
    } else {
        Write-Host "❌ Cannot connect to SendGrid SMTP server" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Network test failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Test Application Configuration
Write-Host "Testing application configuration..." -ForegroundColor Yellow

# Set environment variable for this session
$env:SENDGRID_API_KEY = $ApiKey
$env:MAIL_FROM = $FromEmail

Write-Host "✅ Environment variables set" -ForegroundColor Green

# Test 4: Start Application Test
Write-Host ""
Write-Host "=== Next Steps ===" -ForegroundColor Cyan
Write-Host "1. Start your application:" -ForegroundColor White
Write-Host "   java -jar target/nexsplit-*.jar" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Test email configuration:" -ForegroundColor White
Write-Host "   curl -X GET http://localhost:8080/api/v1/email/config" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Test email sending:" -ForegroundColor White
Write-Host "   curl -X POST http://localhost:8080/api/v1/email/test-simple \`" -ForegroundColor Gray
Write-Host "     -H 'Content-Type: application/json' \`" -ForegroundColor Gray
Write-Host "     -d '{\"to\":\"$ToEmail\",\"subject\":\"SendGrid Test\",\"message\":\"Hello from SendGrid!\"}'" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Or use the test script:" -ForegroundColor White
Write-Host "   .\scripts\test-email.ps1 -Email '$ToEmail'" -ForegroundColor Gray

Write-Host ""
Write-Host "=== SendGrid Setup Complete ===" -ForegroundColor Green
Write-Host "Make sure to verify your sender email in SendGrid dashboard!" -ForegroundColor Yellow
