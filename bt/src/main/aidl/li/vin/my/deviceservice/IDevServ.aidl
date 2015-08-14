// IDevServ.aidl
package li.vin.my.deviceservice;

import li.vin.my.deviceservice.IVinliServiceCallbackBool;
import li.vin.my.deviceservice.IVinliServiceCallbackDtc;
import li.vin.my.deviceservice.IVinliServiceCallbackFloat;
import li.vin.my.deviceservice.IVinliServiceCallbackInt;
import li.vin.my.deviceservice.IVinliServiceCallbackString;

interface IDevServ {
  String observeBool(String chipId, String name, IVinliServiceCallbackBool cb);
  String observeDtc(String chipId, String name, IVinliServiceCallbackDtc cb);
  String observeFloat(String chipId, String name, IVinliServiceCallbackFloat cb);
  String observeInt(String chipId, String name, IVinliServiceCallbackInt cb);
  String observeString(String chipId, String name, IVinliServiceCallbackString cb);

  String resetDtcs(String chipId, IVinliServiceCallbackBool cb);

  String discover(IVinliServiceCallbackDtc cb);

  void cancelOp(String uuid);
}
