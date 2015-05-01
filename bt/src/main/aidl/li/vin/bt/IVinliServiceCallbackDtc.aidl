// IVinliServiceCallbackDtc.aidl
package li.vin.bt;

// Declare any non-default types here with import statements

interface IVinliServiceCallbackDtc {

  void onCompleted();
  void onError(String err);
  void onNext(in List<String> val);

}
