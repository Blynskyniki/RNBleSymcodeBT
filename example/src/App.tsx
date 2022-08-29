import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  PermissionsAndroid,
  FlatList,
  Button,
  TouchableOpacity,
} from 'react-native';
import SymcodeDriver, { Device } from 'react-native-symcode-bt';
import { useEffect, useState } from 'react';

const SymcodeDriverSingleton = new SymcodeDriver();

const search = async () => {
  await PermissionsAndroid.requestMultiple([
    'android.permission.BLUETOOTH_CONNECT',
    'android.permission.BLUETOOTH_SCAN',
    'android.permission.ACCESS_FINE_LOCATION',
  ]);
  await SymcodeDriverSingleton.enableBluetooth();
  return SymcodeDriverSingleton.searchDevices();
};

const renderItem =
  (connected: Device | null, onPress: (item: Device) => void) =>
  ({ item }: { item: Device }) => {
    return (
      <TouchableOpacity
        onPress={() => onPress(item)}
        style={{
          padding: 10,
          borderWidth: 2,
          margin: 10,
          backgroundColor: connected?.mac === item.mac ? 'grey' : 'transparent',
        }}
      >
        <Text>{item.name}</Text>
      </TouchableOpacity>
    );
  };
export default function App() {
  const [result, setResult] = React.useState<Device[]>([]);
  const [connected, setConnected] = React.useState<Device | null>(null);
  const [scanned, setScanned] = useState<Record<'barcode', string> | null>(
    null
  );

  const onConnect = (item: Device) => {
    SymcodeDriverSingleton.pairDevice(item.mac).then(() => {
      SymcodeDriverSingleton.connect(item.mac).then(() => {
        setConnected(item);
      });
    });
  };

  const onSearch = () => {
    console.log('Searching...');
    setResult([]);
    search().then((r) => {
      console.log('find', r);
      setResult(r);
    });
  };

  useEffect(() => {
    SymcodeDriverSingleton.getPairedDevices().then((r) => {
      if (r[0]) {
        setConnected(r[0]);
      }
      setResult((res) => res.concat(r));
    });
  }, []);
  useEffect(() => {
    SymcodeDriverSingleton.enableNotify(async (r) => {
      console.log('notify', r);
      setScanned(r);
    });
  }, [connected?.mac]);
  return (
    <View style={styles.container}>
      <Text>Example</Text>
      <Button title={'Search'} onPress={onSearch} />
      {scanned !== null && <Text>{scanned.barcode}</Text>}
      <FlatList data={result} renderItem={renderItem(connected, onConnect)} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
