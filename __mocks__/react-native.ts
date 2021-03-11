import EventEmitter = require('events');

export const DeviceEventEmitter = new EventEmitter();

export const NativeModules = {
  RnSymcodeBt: {

    scanDevices: jest.fn(),

    connect: jest.fn(),

    disconnect: jest.fn(),

    enableNotify: jest.fn(),

    disableNotify: jest.fn(),
  },
};
