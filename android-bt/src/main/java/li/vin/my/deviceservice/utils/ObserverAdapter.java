package li.vin.my.deviceservice.utils;

import android.util.Log;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
import rx.Observer;

public abstract class ObserverAdapter<T> extends BaseAdapter implements Observer<T> {
  private final List<T> mItems = new ArrayList<T>();
  private final String mTag;

  public ObserverAdapter() {
    mTag = ((Object) this).getClass().getSimpleName();
  }

  private boolean mIsCompleted = false;

  @Override public int getCount() {
    return mItems.size();
  }

  @Override public T getItem(int position) {
    return mItems.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public void onCompleted() {
    mIsCompleted = true;
  }

  @Override public void onError(Throwable e) {
    Log.e(mTag, "error while observing for list", e);
    mIsCompleted = true;
    // TODO: need to display error
  }

  @Override public void onNext(T item) {
    mItems.add(item);
    notifyDataSetChanged();
  }

  public boolean isCompleted() {
    return mIsCompleted;
  }
}
