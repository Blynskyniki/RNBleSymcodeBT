/* tslint:disable:no-console */
import { DeviceEventEmitter, NativeModules } from 'react-native';


const android = NativeModules.RnSymcodeBt;
const BARCODE_SCAN_NOTIFY_EVENT_NAME = 'BARCODE_SCAN_NOTIFY_EVENT';

export type  Device = Record<'name' | 'mac', string>;

export default class Symcode {

  public async scanDevices(): Promise<Device[]> {
    return android.scanDevices();
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
