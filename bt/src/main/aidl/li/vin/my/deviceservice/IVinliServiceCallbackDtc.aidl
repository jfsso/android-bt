// IVinliServiceCallbackDtc.aidl
package li.vin.my.deviceservice;

// Declare any non-default types here with import statements

interface IVinliServiceCallbackDtc {

  void onCompleted();
  void onError(String err);
  void onNext(in List<String> val);

}
