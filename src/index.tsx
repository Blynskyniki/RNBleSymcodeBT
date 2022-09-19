import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-symcode-bt' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const SymcodeBt = NativeModules.SymcodeBt
  ? NativeModules.SymcodeBt
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const BARCODE_SCAN_NOTIFY_EVENT_NAME = 'BARCODE_SCAN_NOTIFY_EVENT';

export type Device = Record<'name' | 'mac', string> &
  Record<'isPaired', boolean>;

export function sleep<T>(ms: number, value?: T) {
  return new Promise<T>((resolve) => {
    setTimeout(() => {
      resolve(value as any);
    }, ms);
  });
}

const ONE_MINUTE = 1000 * 60;

const eventEmitter = new NativeEventEmitter(SymcodeBt);

export default class SymcodeDriver {
  public async enableBluetooth(): Promise<boolean> {
    return SymcodeBt.enableBluetooth();
  }

  public async searchDevices(timeout?: number): Promise<Device[]> {
    return Promise.race([
      SymcodeBt.searchDevices(),
      sleep<Device[]>(timeout || ONE_MINUTE, []),
    ]);
  }

  public async getPairedDevices(timeout?: number): Promise<Device[]> {
    return Promise.race([
      SymcodeBt.getPairedDevices(),
      sleep<Device[]>(timeout || ONE_MINUTE, []),
    ]);
  }

  public async isPaired(mac: string): Promise<boolean> {
    return SymcodeBt.isPaired(mac);
  }

  public async isConnected(mac: string): Promise<boolean> {
    return SymcodeBt.isConnected(mac);
  }

  public async pairDevice(mac: string): Promise<boolean> {
    return SymcodeBt.pairDevice(mac);
  }

  public async connect(mac: string): Promise<boolean> {
    return SymcodeBt.connect(mac);
  }
  // Использовать для подключения в фоне
  public async asyncConnectWithTimeout(mac: string): Promise<void> {
    return SymcodeBt.asyncConnectWithTimeout(mac);
  }

  public async disconnect(): Promise<void> {
    await SymcodeBt.disconnect();
  }

  public async enableNotify(
    eventFn: (data: Record<'barcode', string>) => Promise<void>
  ): Promise<void> {
    await SymcodeBt.enableNotify();
    eventEmitter.addListener(
      BARCODE_SCAN_NOTIFY_EVENT_NAME,
      (barcode: Record<'barcode', string>) => {
        eventFn(barcode);
      }
    );
  }

  public async disableNotify(): Promise<void> {
    await SymcodeBt.disableNotify();
    eventEmitter.removeAllListeners(BARCODE_SCAN_NOTIFY_EVENT_NAME);
  }
}
