# rn-symcode-bt

React native library from Symcode barcodes scaner (SPP protocol)

## Installation

```sh
npm install rn-symcode-bt
```
# Usage :
1. Select SPP mode on scaner:
![spp_img](./img/spp.jpg)

2. Pair your device in standard android
 bluetooth menu
3. device name is BarCode Scanner spp
4. use
```typescript
import Symcode from "rn-symcode-bt";
/**
 * Methods:
 */
export declare type Device = Record<'name' | 'mac', string>;

export default class SymcodeDriver {
  enableBluetooth(): Promise<boolean>;
  isConnected(): Promise<boolean>;
  connect(): Promise<boolean>;
  disconnect(): Promise<void>;
  enableNotify(eventFn: (data: Record<'barcode', string>) => Promise<void>): Promise<void>;
  disableNotify(): Promise<void>;
}

```


## License

MIT
