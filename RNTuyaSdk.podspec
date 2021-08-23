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
    'Resources' => ['ios/RNTuyaSdk/Camera/**/*.{lproj,png,strings,storyboard}']
  }

  s.dependency 'React'
  s.dependency 'TuyaSmartHomeKit','~> 3.28.5'
  s.dependency 'TuyaSmartCameraKit','~> 4.22.0'
  s.dependency 'TuyaCameraUIKit'
  s.dependency 'TYEncryptImage'
  s.dependency 'DACircularProgress'
  s.dependency 'MBProgressHUD', '~> 0.9.2'
  s.dependency 'SDWebImage'
  s.dependency 'ZMJTipView'


end
