import { DeviceEventEmitter, NativeModules } from 'react-native';

const android = NativeModules.RnSymcodeBt;

const BARCODE_SCAN_NOTIFY_EVENT_NAME = 'BARCODE_SCAN_NOTIFY_EVENT';

export type  Device = Record<'name' | 'mac', string> & Record<'isPaired', boolean>;

export function sleep<T>(ms: number, value?: T) {
  return new Promise<T>(resolve => {
    setTimeout(() => {
      resolve(value as any);
    }, ms);
  });
}

const ONE_MINUTE = 1000 * 60;

export default class SymcodeDriver {
  public async enableBluetooth(): Promise<boolean> {
    return android.enableBluetooth();
  }

  public async searchDevices(timeout?: number): Promise<Device[]> {
    return Promise.race([android.searchDevices(), sleep<Device[]>(timeout || ONE_MINUTE, [])]);

  }

  public async getPairedDevices(timeout?: number): Promise<Device[]> {
    return Promise.race([android.getPairedDevices(), sleep<Device[]>(timeout || ONE_MINUTE, [])]);
  }

  public async isPaired(mac: string): Promise<boolean> {
    return android.isPaired(mac);
  }

  public async isConnected(mac: string): Promise<boolean> {
    return android.isConnected(mac);
  }

  public async pairDevice(mac: string): Promise<boolean> {
    return android.pairDevice(mac);
  }

  public async connect(mac: string): Promise<boolean> {
    return android.connect(mac);
  }

  public async disconnect(): Promise<void> {
    await android.disconnect();
  }

  public async enableNotify(eventFn: (data: Record<'barcode', string>) => Promise<void>): Promise<void> {
    await android.enableNotify();
    DeviceEventEmitter.addListener(BARCODE_SCAN_NOTIFY_EVENT_NAME, barcode => {
      eventFn(barcode);
    });
  }

  public async disableNotify(): Promise<void> {
    await android.disableNotify();
    DeviceEventEmitter.removeAllListeners(BARCODE_SCAN_NOTIFY_EVENT_NAME);
  }
}


