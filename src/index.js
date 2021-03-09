/* tslint:disable:no-console */
import { DeviceEventEmitter, NativeModules } from 'react-native';
const android = NativeModules.RnSymcodeBt;
const BARCODE_SCAN_NOTIFY_EVENT_NAME = 'BARCODE_SCAN_NOTIFY_EVENT';
export default class Symcode {
    async scanDevices() {
        return android.scanDevices();
    }
    async connect(mac) {
        return android.connect(mac);
    }
    async disconnect() {
        await android.disconnect();
    }
    async enableNotify(eventFn) {
        await android.enableNotify();
        DeviceEventEmitter.addListener(BARCODE_SCAN_NOTIFY_EVENT_NAME, barcode => {
            eventFn(barcode);
        });
    }
    async disableNotify() {
        await android.disableNotify();
        DeviceEventEmitter.removeAllListeners(BARCODE_SCAN_NOTIFY_EVENT_NAME);
    }
}
//   private _notConnectMessage: IErrorMessage = <IErrorMessage>{
//     TAG_TTK_ERROR_CODE: '1',
//     IS_ERROR: true,
//     TAG_TTK_ERROR_TEXT: `Нет подключения к терминалу`,
//     TAG_TTK_SERVER_MID: 'EXEPTION',
//   };
//   public _listener: IStatusListener;
//   public _timeout: number;
//
//   /**
//    * Инициализатор
//    * @param ip
//    * @param port
//    * @param timeout
//    * @param listener
//    * @returns {Promise<any>}
//    * @param bank
//    */
//   public static async getInstance(
//     ip: string,
//     port: number,
//     timeout: ITimeout,
//     bank: Banks,
//     listener: IStatusListener,
//   ): Promise<PinPadControl> {
//     const instance = new this();
//     instance._listener = listener;
//     instance._timeout = timeout.operationTimeout;
//     await instance.settings(ip, port, timeout.connectTimeout, bank);
//     return instance;
//   }
//
//   /**
//    * Проверка на ошибку выключения wifi
//    * @param response
//    * @return {boolean}
//    */
//   public static isConnectionAbort(response: IErrorMessage): boolean {
//     return response && response.TAG_TTK_ERROR_CODE === CONNECTION_ABORT_CODE;
//   }
//
//   /**
//    * Проверка на ти сообщения IErrorMessage
//    * @param response
//    * @return {any | boolean}
//    */
//   public static isErrorMessage(response?): response is IErrorMessage {
//     return response && (response as object).hasOwnProperty('IS_ERROR');
//   }
//
//   /**
//    * Проверка на ти сообщения IInfoMessage
//    * @param response
//    * @return {any | boolean}
//    */
//   public static isInsfoMessage(response?): response is IInfoMessage {
//     return (
//       response && (response as object).hasOwnProperty('TAG_TTK_SERVER_MID') && response.TAG_TTK_SERVER_MID === 'INF'
//     );
//   }
//
//   /**
//    * Проверка на ти сообщения IStandartMessage
//    * @param response
//    * @return {any | boolean}
//    */
//   public static isStandartMessage(response?): response is IStandartMessage {
//     return (
//       response && (response as object).hasOwnProperty('TAG_TTK_SERVER_MID') && response.TAG_TTK_SERVER_MID !== 'INF'
//     );
//   }
//
//   /**
//    * Настройка подключения
//    * @param ip
//    * @param port
//    * @param timeout
//    * @param bank
//    * @return {Promise<void>}
//    */
//   private async settings(ip: string, port: number, timeout: number, bank: Banks): Promise<void> {
//     await android.setup(ip, port, timeout, bank);
//   }
//
//   /**
//    * Удаление слушателей
//    * @returns void
//    */
//   private removeAllListeners() {
//     DeviceEventEmitter.removeAllListeners('OnPinPadStatus');
//   }
//
//   /**
//    * Выставить оплату
//    * @param {number} amount
//    * @param {number} clientNumber
//    * @param {number} checkNumber
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    * @param requestID
//    */
//   @Timeout
//   @StatusListener
//   public async payment(
//     amount: number,
//     clientNumber: number,
//     checkNumber: number,
//     requestID: number,
//   ): Promise<IStandartMessage | IErrorMessage> {
//     if (amount < 1) {
//       return this._notConnectMessage;
//     }
//     return <IStandartMessage | IErrorMessage>android.payment(amount, clientNumber, checkNumber, requestID);
//   }
//
//   /**
//    * Получить данные карты (Для спасибо!!!)
//    * @param {number} clientNumber
//    * @param {number} checkNumber
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    * @param requestID
//    */
//   @Timeout
//   @StatusListener
//   public async getCardInfo(
//     clientNumber: number,
//     checkNumber: number,
//     requestID: number,
//   ): Promise<IStandartMessage | IErrorMessage> {
//     return <IStandartMessage | IErrorMessage>await android.getCardInfo(clientNumber, checkNumber, requestID);
//   }
//
//   /**
//    *  Информация о транзакции сохраненной в журнале по REQUEST_ID
//    * @param amount
//    * @param clientNumber
//    * @param checkNumber
//    * @param searchRequestID
//    * @param requestID
//    * @return {Promise<IStandartMessage | IErrorMessage>}
//    */
//   @Timeout
//   @StatusListener
//   public async getInfoByRequestId(
//     amount: number,
//     clientNumber: number,
//     checkNumber: number,
//     searchRequestID: number,
//     requestID: number,
//   ): Promise<IStandartMessage | IErrorMessage> {
//     return <IStandartMessage | IErrorMessage>(
//       await android.getInfoByRequestId(amount, clientNumber, checkNumber, searchRequestID, requestID)
//     );
//   }
//
//   /**
//    * Возврат
//    * @param rrn
//    * @param amount
//    * @param clientNumber
//    * @param checkNumber
//    * @param requestID
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    */
//   @Timeout
//   @StatusListener
//   public async refund(
//     rrn: string,
//     amount: number,
//     clientNumber: number,
//     checkNumber: number,
//     requestID: number,
//   ): Promise<IStandartMessage | IErrorMessage> {
//     if (amount < 1) {
//       return <IErrorMessage>{
//         TAG_TTK_ERROR_CODE: '1',
//         IS_ERROR: true,
//         TAG_TTK_ERROR_TEXT: `Неверная сумма оплаты => ${amount}`,
//         TAG_TTK_SERVER_MID: 'EXEPTION',
//       };
//     }
//     return <IStandartMessage | IErrorMessage>await android.refund(amount, rrn, clientNumber, checkNumber, requestID);
//   }
//
//   /**
//    * Отмена оплаты
//    * @param {number} amount
//    * @param {string} rrn - номер ссылки
//    * @param {number} clientNumber
//    * @param {number} checkNumber
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    * @param requestID
//    */
//   @Timeout
//   @StatusListener
//   public async cancelPayment(
//     amount: number,
//     rrn: string,
//     clientNumber: number,
//     checkNumber: number,
//     requestID: number,
//   ): Promise<IStandartMessage | IErrorMessage> {
//     if (amount < 1) {
//       return <IErrorMessage>{
//         TAG_TTK_ERROR_CODE: '1',
//         IS_ERROR: true,
//         TAG_TTK_ERROR_TEXT: `Неверная сумма оплаты => ${amount}`,
//         TAG_TTK_SERVER_MID: 'EXEPTION',
//       };
//     }
//     return <IStandartMessage | IErrorMessage>await android.cancelPayment(amount, rrn, clientNumber, checkNumber, requestID);
//   }
//
//   /**
//    * Сверка итогов (журнал терминала)
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    */
//   @Timeout
//   @StatusListener
//   public async reconciliation(): Promise<IStandartMessage | IErrorMessage> {
//     return await android.reconciliation();
//   }
//
//   /**
//    * Отмена операции по номеру.
//    * Операция прекращается, если такой операции не происходило, то игрорируется.
//    * @param {number} clientNumber
//    * @param  {number} checkNumber
//    * @returns {Promise<any>}
//    */
//   @Timeout
//   @StatusListener
//   public async abortOperation(clientNumber: number, checkNumber: number): Promise<IStandartMessage | IErrorMessage> {
//     return await android.abortOperation(clientNumber, checkNumber);
//   }
//
//   /**
//    * Отчет по проведенным операциям без гашения (сверки итогов)
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    */
//   @Timeout
//   @StatusListener
//   public async eft(): Promise<IStandartMessage | IErrorMessage> {
//     return await android.eft();
//   }
//
//   /**
//    * Открытие сервисного меню терминала (там много всего),
//    * метод ожидает результат от операций из меню.
//    * @returns {Promise<IStandartMessage | IErrorMessage>}
//    */
//   @Timeout
//   @StatusListener
//   public async openServiceMenu(): Promise<IStandartMessage | IErrorMessage> {
//     return await android.openServiceMenu();
//   }
//
//   /**
//    * Прослушивание статусов состояния от терминала.
//    * @param  {IStatusListener} job
//    * @param dispatch
//    */
//   private addStatusListener(job: IStatusListener, dispatch): void {
//     DeviceEventEmitter.addListener('OnPinPadStatus', m => {
//       job.messageReceived(m);
//     });
//   }
// }
