// IVinliServiceCallbackString.aidl
package li.vin.my.deviceservice;

interface IVinliServiceCallbackString {

  void onCompleted();
  void onError(String err);
  void onNext(String val);

}
