// IVinliServiceCallbackFloat.aidl
package li.vin.bt;

interface IVinliServiceCallbackFloat {

  void onCompleted();
  void onError(String err);
  void onNext(float val);

}
