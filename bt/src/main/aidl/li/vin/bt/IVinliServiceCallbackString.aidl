// IVinliServiceCallbackString.aidl
package li.vin.bt;

interface IVinliServiceCallbackString {

  void onCompleted();
  void onError(String err);
  void onNext(String val);

}
