# rn-symcode-bt

React native library from Symcode barcodes scaner

## Installation

```sh
npm install rn-symcode-bt
```

## Select BLE mode on scaner
![alt text](./img/ble.jpg)

## Usage

```typescript
import Symcode from "rn-symcode-bt";
/**
 * Methods:
 */
export declare type Device = Record<'name' | 'mac', string>;
export default class Symcode {
  scanDevices(): Promise<Device[]>;
  connect(mac: string): Promise<boolean>;
  disconnect(): Promise<void>;
  enableNotify(eventFn: (data: Record<'barcode', string>) => Promise<void>): Promise<void>;
  disableNotify(): Promise<void>;
}
```


## License

MIT
