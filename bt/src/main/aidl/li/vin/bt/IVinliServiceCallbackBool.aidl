// IVinliServiceCallbackBool.aidl
package li.vin.bt;

interface IVinliServiceCallbackBool {

  void onCompleted();
  void onError(String err);
  void onNext(boolean val);

}
