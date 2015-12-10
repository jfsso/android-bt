Vinli Android Bluetooth SDK
===========================

> This SDK and accompanying documentation is a work in progress.
> There are areas that are still to be completed and finalized.
> We welcome your feedback. Please post any errors as issues on GitHub and Vinli engineering will respond as quickly as possible.
> And stay tuned for updates over the next few weeks as we bring more features to the SDK.

An Android client for interacting with the Vinli bluetooth device from within your application.
[Sample App](https://github.com/vinli/android-techcrunch-demo)

Build

-----------

[![Build Status](https://travis-ci.org/vinli/android-bt.svg?branch=master)](https://travis-ci.org/vinli/android-bt)

Conventions
-----------
### [RxJava](https://github.com/ReactiveX/RxJava/wiki)
The developer interfaces with the SDK using reactive Observables and Subscriptions.
All data from the device is streamed via Observable, and the data stream is stopped by unsubscribing from the subscription.

Docs
----

### [JavaDocs](http://vinli.github.io/android-bt/)

Standard Flow
-------------
### Obtain Device Unlock Key
TODO: will be needed in the future, but dev devices are currently unlocked.

### Subscribe to Data
```java
  List<Subscription> subscriptions = new ArrayList<>();
  Fragment self = this;

  final DeviceConnection deviceConn = VinliDevices.createDeviceConnection(getActivity(), "123123");

  Subscription rpmSub = AppObservable
    .bindFragment(self, deviceConn.observe(Params.RPM))
    .subscribe(new Subscriber<Float>() {
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
