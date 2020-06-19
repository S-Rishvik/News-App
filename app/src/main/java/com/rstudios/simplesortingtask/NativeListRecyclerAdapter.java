package com.rstudios.simplesortingtask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdsManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class NativeListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int AD_DISPLAY_FREQUENCY = 5;
    private Context context;
    private ArrayList<ListItem> arrayList;
    public static final int AD_TYPE=0;
    public static final int POST_TYPE=1;
    private Activity mActivity;
    private List<NativeAd> mAdItems;
    private NativeAdsManager mNativeAdsManager;

    public NativeListRecyclerAdapter(Context context, ArrayList<ListItem> arrayList, Activity mActivity, NativeAdsManager mNativeAdsManager) {
        this.context = context;
        this.arrayList = arrayList;
        this.mActivity = mActivity;
        mAdItems = new ArrayList<>();
        this.mNativeAdsManager = mNativeAdsManager;
    }

    @Override
    public int getItemViewType(int position) {
        return position % AD_DISPLAY_FREQUENCY == 0 ? AD_TYPE : POST_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AD_TYPE) {
            NativeAdLayout inflatedView = (NativeAdLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new AdHolder(inflatedView);
        } else {
            View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list,parent,false);
            return new PostHolder(inflatedView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == AD_TYPE) {
            NativeAd ad;

            if (mAdItems.size() > position / AD_DISPLAY_FREQUENCY) {
                ad = mAdItems.get(position / AD_DISPLAY_FREQUENCY);
            } else {
                ad = mNativeAdsManager.nextNativeAd();
                if (!ad.isAdInvalidated()) {
                    mAdItems.add(ad);
                } else {
                    Log.w(NativeListRecyclerAdapter.class.getSimpleName(), "Ad is invalidated!");
                }
            }

            AdHolder adHolder = (AdHolder) holder;
            adHolder.adChoicesContainer.removeAllViews();

            if (ad != null) {

                adHolder.tvAdTitle.setText(ad.getAdvertiserName());
                adHolder.tvAdBody.setText(ad.getAdBodyText());
                adHolder.tvAdSocialContext.setText(ad.getAdSocialContext());
                adHolder.tvAdSponsoredLabel.setText("Sponsored");
                adHolder.btnAdCallToAction.setText(ad.getAdCallToAction());
                adHolder.btnAdCallToAction.setVisibility(
                        ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                AdOptionsView adOptionsView =
                        new AdOptionsView(mActivity, ad, adHolder.nativeAdLayout);
                adHolder.adChoicesContainer.addView(adOptionsView, 0);

                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(adHolder.ivAdIcon);
                clickableViews.add(adHolder.mvAdMedia);
                clickableViews.add(adHolder.btnAdCallToAction);
                ad.registerViewForInteraction(adHolder.nativeAdLayout, adHolder.mvAdMedia, adHolder.ivAdIcon, clickableViews);
            }
        } else {
            PostHolder postHolder = (PostHolder) holder;
            int index = position - (position / AD_DISPLAY_FREQUENCY) - 1;
            ListItem listItem=arrayList.get(index);
            postHolder.title.setText(listItem.getTitle());
            postHolder.desc.setText(listItem.getDesc());
            postHolder.source.setText(listItem.getSource());
            Glide.with(context).load(listItem.getImgUrl()).placeholder(R.drawable.ic_image).centerCrop().into(postHolder.imageView);
            postHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(context,NewsActivity.class);
                    intent.putExtra("url",listItem.getPageUrl());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size() + mAdItems.size();
    }
}

class PostHolder extends RecyclerView.ViewHolder{
    TextView title,desc,source;
    ShapeableImageView imageView;
    View view;
    public PostHolder(@NonNull View itemView) {
        super(itemView);
        title=itemView.findViewById(R.id.item_title);
        desc=itemView.findViewById(R.id.item_desc);
        source=itemView.findViewById(R.id.item_source);
        imageView=itemView.findViewById(R.id.item_image);
        view=itemView;
    }
}


class AdHolder extends RecyclerView.ViewHolder {

    NativeAdLayout nativeAdLayout;
    MediaView mvAdMedia;
    MediaView ivAdIcon;
    TextView tvAdTitle;
    TextView tvAdBody;
    TextView tvAdSocialContext;
    TextView tvAdSponsoredLabel;
    Button btnAdCallToAction;
    LinearLayout adChoicesContainer;

    AdHolder(NativeAdLayout adLayout) {
        super(adLayout);
        nativeAdLayout = adLayout;
        mvAdMedia = adLayout.findViewById(R.id.native_ad_media);
        tvAdTitle = adLayout.findViewById(R.id.native_ad_title);
        tvAdBody = adLayout.findViewById(R.id.native_ad_body);
        tvAdSocialContext = adLayout.findViewById(R.id.native_ad_social_context);
        tvAdSponsoredLabel = adLayout.findViewById(R.id.native_ad_sponsored_label);
        btnAdCallToAction = adLayout.findViewById(R.id.native_ad_call_to_action);
        ivAdIcon = adLayout.findViewById(R.id.native_ad_icon);
        adChoicesContainer = adLayout.findViewById(R.id.ad_choices_container);
    }
}
