import { NativeModules } from 'react-native';

const tuya = NativeModules.TuyaCameraModule;

export type CameraLivePreviewParams = {
  countryCode: string;
  uid: string;
  passwd: string;
  devId: string;
  options: [string];
};

export function openCameraLivePreview(params: CameraLivePreviewParams): Promise<string> {
  return tuya.openLivePreview(params);
}
