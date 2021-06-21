import { DeviceEventEmitter, NativeModules } from 'react-native';

const android = NativeModules.RnSymcodeBt;

const BARCODE_SCAN_NOTIFY_EVENT_NAME = 'BARCODE_SCAN_NOTIFY_EVENT';

export type  Device = Record<'name' | 'mac' | 'bondState', string>;

export default class SymcodeDriver {
  public async enableBluetooth(): Promise<boolean> {
    return android.enableBluetooth();
  }

  public async searchDevices(): Promise<Device[]> {
    return android.searchDevices();
  }

  public async isPaired(mac: string): Promise<boolean> {
    return android.isPaired(mac);
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


