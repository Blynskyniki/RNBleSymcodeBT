# rn-symcode-bt

React native library for Symcode barcodes scaner (or others) (SPP protocol)

## Installation

```sh
npm install rn-symcode-bt
```

# Usage :

1. Select SPP mode on scaner (for others read documentation of concrete device):
   ![spp_img](./img/spp.jpg)

2. Pair your device in standard android bluetooth menu (or use methods: *serachDevices* and *pairDevice*)
3. Connect device, use *connect* or *asyncConnectWithTimeout* methods
4. For notify of scan event, use *enableNotify*

```typescript
import Symcode from "rn-symcode-bt";

/**
 * Methods:
 */
export declare type Device = Record<'name' | 'mac', string> & Record<'isPaired', boolean>;

export default class SymcodeDriver {
  enableBluetooth(): Promise<boolean>;

  searchDevices(timeout?: number): Promise<Device[]>;

  getPairedDevices(timeout?: number): Promise<Device[]>;

  isPaired(mac: string): Promise<boolean>;

  isConnected(mac: string): Promise<boolean>;

  pairDevice(mac: string): Promise<boolean>;

  connect(mac: string): Promise<boolean>;

  asyncConnectWithTimeout(mac: string): Promise<void>;

  disconnect(): Promise<void>;

  enableNotify(eventFn: (data: Record<'barcode', string>) => Promise<void>): Promise<void>;

  disableNotify(): Promise<void>;
}


```

## License

MIT
