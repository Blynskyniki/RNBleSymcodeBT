import Symcode from './index'
import { DeviceEventEmitter, NativeModules } from 'react-native';

const android = NativeModules.RnSymcodeBt;

jest.mock('react-native')

const spy = jest.spyOn(Symcode.prototype,'connect')
describe('tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });


  test('call constructor and connect',async ()=>{

    const instance =  new Symcode();
    await instance.connect('fake_mac');

    expect(spy).toBeCalled()


  })


});
