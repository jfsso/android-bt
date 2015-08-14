// IVinliServiceCallbackBool.aidl
package li.vin.my.deviceservice;

interface IVinliServiceCallbackBool {

  void onCompleted();
  void onError(String err);
  void onNext(boolean val);

}
