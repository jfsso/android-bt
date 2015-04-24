Vinli Android Bluetooth SDK
===========================

> This SDK and accompanying documentation is a work in progress.
> There are areas that are still to be completed and finalized.
> We welcome your feedback. Please post any errors as issues on GitHub and Vinli engineering will respond as quickly as possible.
> And stay tuned for updates over the next few weeks as we bring more features to the SDK.

An Android client for interacting with the Vinli bluetooth device from within your application.
[Sample App](https://github.com/vinli/android-demo)

Conventions
-----------
### [RxJava](https://github.com/ReactiveX/RxJava/wiki)
The developer interfaces with the SDK using reactive Observables and Subscriptions.
All data from the device is streamed via Observable, and the data stream is stopped by unsubscribing from the subscription.

Standard Flow
-------------
### Obtain Device Unlock Key
TODO: will be needed in the future, but dev devices are currently unlocked.

### Device Discovery
Find Vinli Devices in range.
```java
// from fragment, to create a device connection observable that can be shared among data requests
Observable<DeviceConnection> deviceConnObservable = AppObservable.bindFragment(this, VinliDevices.createDeviceObservable(getActivity()))
  .first() // take the first device discovered
  .map(new Func1<Device, DeviceConnection>() { // connect to the device
    @Override public DeviceConnection call(Device device) {
      return device.createDeviceConnection(getActivity(), "123123" /*device unlock code received from backend*/);
    }
  })
  .share();
```

### Subscribe to Data
```java
  List<Subscription> subscriptions = new ArrayList<>();
  Fragment self = this;
  
  Subscription rpmSub = deviceConnObservable..flatMap(new Func1<DeviceConnection, Observable<Float>>() {
    @Override public Observable<Float> call(DeviceConnection deviceConn) {
      return AppObservable.bindFragment(self, deviceConn.observe(Params.RPM));
    }
  }).subscribe(new Subscriber<Float>() {
    @Override public void onCompleted() {
      // end of stream reached
    }

    @Override public void onError(Throwable e) {
      // and error happened
    }

    @Override public void onNext(Float val) {
      // next value available
    }
  });
  
  subscriptions.add(rpmSub);
```

### Unsubscribe When Finished
```java
  for (Subscription s : subscriptions) {
    s.unsubscribe();
  }
  subscriptions.clear();
```
