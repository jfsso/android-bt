// IVinliServiceCallbackInt.aidl
package li.vin.my.deviceservice;

interface IVinliServiceCallbackInt {

  void onCompleted();
  void onError(String err);
  void onNext(int val);

}
