package com.wb.nextgenlibrary.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.wb.nextgenlibrary.R;
import com.wb.nextgenlibrary.analytic.NextGenAnalyticData;
import com.wb.nextgenlibrary.util.utils.F;
import com.wb.nextgenlibrary.util.utils.NextGenLogger;

/**
 * Created by gzcheng on 4/11/16.
 */
public abstract class AbstractNextGenFragment extends Fragment {
    View view;
    ImageButton closeBtn;
    boolean shouldShowCloseBtn = false;
    boolean shouldShowFullScreenBtn = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(getContentViewId(), container, false);
        } catch (InflateException e) {
            NextGenLogger.e(F.TAG, e.getLocalizedMessage());
        }
        return view;

    }

    protected int getCloseButtonId(){   // override this if for some reasons your close button's ID is different from this
        return R.id.close_button;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        closeBtn = (ImageButton) view.findViewById(getCloseButtonId());
        if (closeBtn != null) {
            if (shouldShowCloseBtn) {
                closeBtn.setVisibility(View.VISIBLE);

            }else {
                closeBtn.setVisibility(View.GONE);
            }
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        NextGenAnalyticData.reportEvent(getActivity(), AbstractNextGenFragment.this, "Back Button",
                                NextGenAnalyticData.AnalyticAction.ACTION_CLICK, null);
                        getActivity().onBackPressed();
                    }
                }
            });
        }
        NextGenAnalyticData.reportEvent(getActivity(), this, null, NextGenAnalyticData.AnalyticAction.ACTION_START, getReportContentName());
    }

    abstract String getReportContentName();

    public void setShouldShowCloseBtn(boolean bShow){
        shouldShowCloseBtn = bShow;
        if (closeBtn != null){
            closeBtn.setVisibility(bShow? View.VISIBLE : View.GONE);
        }
    }

    public void setShouldShowFullScreenBtn(boolean bShow){
        shouldShowFullScreenBtn = bShow;
    }

    abstract int getContentViewId();

}
