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
public async scanDevices(): Promise<Device[]>
public async connect(mac: string): Promise<boolean>
public async disconnect(): Promise<void>
public async enableNotify(eventFn: (data: Record<'barcode', string>) => Promise<void>): Promise<void>
public async disableNotify(): Promise<void>
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
