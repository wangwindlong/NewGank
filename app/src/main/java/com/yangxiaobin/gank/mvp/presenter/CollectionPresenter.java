package com.yangxiaobin.gank.mvp.presenter;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.yangxiaobin.Constant;
import com.yangxiaobin.gank.App;
import com.yangxiaobin.gank.R;
import com.yangxiaobin.gank.common.base.BasePresenter;
import com.yangxiaobin.gank.common.bean.ContentItemEntity;
import com.yangxiaobin.gank.mvp.contract.CollectionContract;
import com.yangxiaobin.gank.mvp.view.Itemtype.ItemTypeContentCategoryContent;
import com.yangxiaobin.gank.mvp.view.Itemtype.ItemTypeContentCategoryTitle;
import com.yangxiaobin.gank.mvp.view.activity.LandscapeVideoActivity;
import com.yangxiaobin.gank.mvp.view.adapter.ContentAdapter;
import com.yangxiaobin.gank.mvp.view.adapter.FlagForContentAdapter;
import com.yangxiaobin.gank.mvp.view.fragment.CategoryFragment;
import com.yangxiaobin.gank.mvp.view.fragment.PicDialogFragment;
import com.yangxiaobin.gank.mvp.view.fragment.WebFragment;
import com.yxb.base.CommonKey;
import com.yxb.base.utils.ActivitySkipper;
import com.yxb.base.utils.FragmentSkipper;
import com.yxb.easy.adapter.AdapterWrapper;
import com.yxb.easy.listener.OnItemClickListener;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by handsomeyang on 2017/8/15.
 */

public class CollectionPresenter extends BasePresenter
    implements CollectionContract.Presenter, View.OnClickListener, OnItemClickListener {

  private CollectionContract.Model mModel;
  private CollectionContract.View mView;

  private PicDialogFragment mPicDialogFragment;     // 图片放大dialog
  private List<ContentItemEntity> mContentItemEntities;
  private AdapterWrapper mAdapterWrapper;

  @Inject public CollectionPresenter(CollectionContract.Model model, CollectionContract.View view) {
    mModel = model;
    mView = view;
  }

  @Override public void onDestroy() {
    mView.getRealmHelper().closeRealm();
  }

  @Override public void start() {
    // 有title 有items
    mContentItemEntities = getDataFromDB();
    ContentAdapter adapter =
        new ContentAdapter(mContentItemEntities, FlagForContentAdapter.COLLECTION);

    mAdapterWrapper = new AdapterWrapper(adapter);
    adapter.addItemViewDelegate(new ItemTypeContentCategoryTitle());
    adapter.addItemViewDelegate(new ItemTypeContentCategoryContent(mAdapterWrapper));

    View emptyView = LayoutInflater.from(mView.getViewContext())
        .inflate(R.layout.layout_empty_list, mView.getEmptyParent(), false);
    mAdapterWrapper.setEmptyView(emptyView);

    mView.setRecyclerViewAdapter(mAdapterWrapper);

    mPicDialogFragment = new PicDialogFragment();
  }

  // navigation click listener
  @Override public void onClick(View v) {
    mView.removeSelf();
  }

  private List<ContentItemEntity> getDataFromDB() {
    return mView.getRealmHelper().getAllSortedContentItemEntities();
  }

  @Override public void onItemClick(View view, int pos, MotionEvent motionEvent) {
    // content
    ContentItemEntity entity = mContentItemEntities.get(pos);
    switch (view.getId()) {
      case R.id.layout_title_content_fragment:
        // title  start category fragment
        if (view.getId() == R.id.layout_title_content_fragment) {
          TextView textView = view.findViewById(R.id.tv_item_title_content_fragment);
          FragmentSkipper.getInstance()
              .init(mView.getViewContext())
              .target(new CategoryFragment())
              .putString(CommonKey.STR1, textView.getText().toString().trim())
              .add(android.R.id.content, true);
        }
        break;
      case R.id.layout_item_content_fragment:
        startWebFragment(entity, pos);
        break;
      case R.id.imgv1_item_content_content_fragment:
        String url = entity.getImages().get(1);
        if (!TextUtils.isEmpty(url)) {
          mPicDialogFragment.setUrl(url);
          showPicDialog();
        }
        break;
      case R.id.imgv2_item_content_content_fragment:
        mPicDialogFragment.setUrl(entity.getImages().get(0));
        showPicDialog();
        break;
      default:
        break;
    }
  }

  private void startWebFragment(ContentItemEntity entity, int pos) {
    // 跳转webFragment
    if (Constant.Category.VIDEO.equals(entity.getType())) {
      startVideoActivity(entity);
    } else {
      FragmentSkipper.getInstance()
          .init(mView.getViewContext())
          .target(new WebFragment().setUrl(entity.getUrl()).setTitle(entity.getDesc()))
          .add(android.R.id.content, true);
    }
    App.getINSTANCE().getItemUrls().add(entity.getUrl());
    mAdapterWrapper.notifyItemChanged(pos);
  }

  // 跳转横屏activity播放
  private void startVideoActivity(ContentItemEntity entity) {
    ActivitySkipper.getInstance()
        .init(mView.getViewContext())
        .putExtras(CommonKey.STR1, entity.getUrl())
        .putExtras(CommonKey.STR2, entity.getDesc())
        .skip(LandscapeVideoActivity.class);
  }

  private void showPicDialog() {
    mPicDialogFragment.show(((FragmentActivity) mView.getViewContext()).getSupportFragmentManager(),
        mPicDialogFragment.getClass().getSimpleName());
  }
}
