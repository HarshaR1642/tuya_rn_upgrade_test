// @flow Copyright © 2019 Rently Softwares, All Rights Reserved

import { NativeModules } from 'react-native';
import { DeviceDetailResponse } from './home';

const tuya = NativeModules.TuyaActivatorModule;

export function openNetworkSettings() {
  return tuya.openNetworkSettings({});
}

export type InitActivatorParams = {
  homeId: number,
  ssid: string,
  password: string,
  time: number,
  type: 'TY_EZ' | 'TY_AP' | 'TY_QR'
};

export type GetTokenParams = {
  homeId: string,
  time: number
};

export type InitActivatorQRCodeParams = {
  ssid: string,
  password: string,
  time: number,
  token: string
};

export type InitGwActivatorParams = {
  homeId: number,
  time: number
};

export function initActivator(
  params: InitActivatorParams
): Promise<DeviceDetailResponse> {
  return tuya.initActivator(params);
}

export function getTokenForQRCode (
  params: GetTokenParams
): Promise<DeviceDetailResponse> {
  return tuya.getTokenForQRCode(params);
}

export function initActivatorForQRCode(
  params: InitActivatorQRCodeParams
): Promise<DeviceDetailResponse> {
  return tuya.initActivatorForQRCode(params);
}

export function newGwActivator(
  params: InitGwActivatorParams
): Promise<DeviceDetailResponse> {
  return tuya.newGwActivator(params);
}

export function stopConfig() {
  return tuya.stopConfig();
}

export function getCurrentWifi(
  success: (ssid: string) => void,
  error: () => void
) {
  // We need the Allow While Using App location permission to use this.
  return tuya.getCurrentWifi({}, success, error);
}
