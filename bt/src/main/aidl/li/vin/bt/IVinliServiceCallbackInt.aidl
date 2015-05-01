// IVinliServiceCallbackInt.aidl
package li.vin.bt;

interface IVinliServiceCallbackInt {

  void onCompleted();
  void onError(String err);
  void onNext(int val);

}
