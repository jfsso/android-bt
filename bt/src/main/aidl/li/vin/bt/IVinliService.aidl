// IVinliService.aidl
package li.vin.bt;

import li.vin.bt.IVinliServiceCallbackBool;
import li.vin.bt.IVinliServiceCallbackDtc;
import li.vin.bt.IVinliServiceCallbackFloat;
import li.vin.bt.IVinliServiceCallbackInt;
import li.vin.bt.IVinliServiceCallbackString;

interface IVinliService {

  void observeBool(String name, IVinliServiceCallbackBool cb);
  void observeDtc(String name, IVinliServiceCallbackDtc cb);
  void observeFloat(String name, IVinliServiceCallbackFloat cb);
  void observeInt(String name, IVinliServiceCallbackInt cb);
  void observeString(String name, IVinliServiceCallbackString cb);

  void resetDtcs(IVinliServiceCallbackBool cb);

  void unsubscribeBool(IVinliServiceCallbackBool cb);
  void unsubscribeDtc(IVinliServiceCallbackDtc cb);
  void unsubscribeFloat(IVinliServiceCallbackFloat cb);
  void unsubscribeInt(IVinliServiceCallbackInt cb);
  void unsubscribeString(IVinliServiceCallbackString cb);

}
