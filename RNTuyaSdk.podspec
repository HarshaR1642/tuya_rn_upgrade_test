require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "RNTuyaSdk"
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platforms    = { :ios => "9.0" }

  s.source       = { :git => "https://github.com/Volst/react-native-tuya.git", :tag => "v#{s.version}" }
  s.source_files  = "ios/**/*.{h,m}"
  s.resource_bundles = {
    'Resources' => ['ios/RNTuyaSdk/Camera/IPC/Assets/**/*.{lproj,png,strings}']
  }

  s.dependency 'React'
  s.dependency 'TuyaSmartHomeKit', '~> 3.17.6'
  s.dependency 'TuyaCameraUIKit'
  s.dependency 'TuyaSmartCloudServiceBizBundle'
  s.dependency 'TuyaSmartCameraKit'
  s.dependency 'TYEncryptImage'
  s.dependency 'DACircularProgress'
  s.dependency 'MBProgressHUD', '~> 0.9.2'


end
