// Generated code from Butter Knife. Do not modify!
package com.suppresswarnings.android;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.suppresswarnings.android.view.MyWebview;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding<T extends MainActivity> implements Unbinder {
  protected T target;

  private View view2131296258;

  @UiThread
  public MainActivity_ViewBinding(final T target, View source) {
    this.target = target;

    View view;
    target.webview = Utils.findRequiredViewAsType(source, R.id.webview, "field 'webview'", MyWebview.class);
    view = Utils.findRequiredView(source, R.id.btn_control, "field 'control' and method 'onButterKnifeBtnClick'");
    target.control = Utils.castView(view, R.id.btn_control, "field 'control'", Button.class);
    view2131296258 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onButterKnifeBtnClick(p0);
      }
    });
    target.warning = Utils.findRequiredViewAsType(source, R.id.tv_warning, "field 'warning'", TextView.class);
    target.background = Utils.findRequiredViewAsType(source, R.id.background, "field 'background'", LinearLayout.class);
    target.progressBar = Utils.findRequiredViewAsType(source, R.id.progressbar, "field 'progressBar'", ProgressBar.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.webview = null;
    target.control = null;
    target.warning = null;
    target.background = null;
    target.progressBar = null;

    view2131296258.setOnClickListener(null);
    view2131296258 = null;

    this.target = null;
  }
}
