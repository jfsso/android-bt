// IVinliServiceCallbackFloat.aidl
package li.vin.my.deviceservice;

interface IVinliServiceCallbackFloat {

  void onCompleted();
  void onError(String err);
  void onNext(float val);

}
