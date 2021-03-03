import { NativeModules } from 'react-native';

type RnSymcodeBtType = {
  multiply(a: number, b: number): Promise<number>;
};

const { RnSymcodeBt } = NativeModules;

export default RnSymcodeBt as RnSymcodeBtType;
