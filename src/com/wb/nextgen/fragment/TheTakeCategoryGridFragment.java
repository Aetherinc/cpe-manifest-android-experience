package com.wb.nextgen.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wb.nextgen.NextGenApplication;
import com.wb.nextgen.R;
import com.wb.nextgen.data.TheTakeData;
import com.wb.nextgen.interfaces.NextGenFragmentTransactionInterface;
import com.wb.nextgen.network.TheTakeApiDAO;
import com.wb.nextgen.util.PicassoTrustAll;
import com.wb.nextgen.util.concurrent.ResultListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gzcheng on 4/11/16.
 */
public class TheTakeCategoryGridFragment extends AbstractNextGenFragment{
    GridView itemsGridView;
    TheTakeProductsGridViewAdapter itemsGridViewAdaptor;
    TheTakeData.TheTakeCategory selectedCategory = null;

    public int getContentViewId(){
        return R.layout.the_take_category_right_grid;
    }

    public void refreshWithCategory(TheTakeData.TheTakeCategory category){
        selectedCategory = category;

        if (selectedCategory.products == null) {


            TheTakeApiDAO.getCategoryProducts(selectedCategory.categoryId, new ResultListener<List<TheTakeData.TheTakeProduct>>() {
                @Override
                public void onResult(List<TheTakeData.TheTakeProduct> result) {
                    selectedCategory.products = result;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (itemsGridViewAdaptor != null)
                                itemsGridViewAdaptor.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public <E extends Exception> void onException(E e) {

                }
            });
        }else {
            if (itemsGridViewAdaptor != null)
                itemsGridViewAdaptor.notifyDataSetChanged();
        }


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itemsGridView = (GridView)view.findViewById(R.id.the_take_items_grid);

        if (itemsGridView != null){
            float density = NextGenApplication.getScreenDensity(getActivity());
            int spacing = (int)(10 *density);
            itemsGridView.setNumColumns(3);
            itemsGridView.setHorizontalSpacing(spacing);
            itemsGridView.setVerticalSpacing(spacing);
            itemsGridView.setPadding(spacing, 0, spacing, spacing);

            itemsGridViewAdaptor = new TheTakeProductsGridViewAdapter();
            itemsGridView.setAdapter(itemsGridViewAdaptor);
            itemsGridView.setOnItemClickListener(itemsGridViewAdaptor);

        }
    }

    public class TheTakeProductsGridViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        //protected NextGenExtraActivity activity;

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= getCount() || position < 0){
                return null;
            }


            if (convertView == null  ) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.the_take_grid_item, parent, false);


            } else {

            }

            ImageView productThumbnail = (ImageView)convertView.findViewById(R.id.the_take_item_thumbnail);
            TextView brandName = (TextView) convertView.findViewById(R.id.the_take_brand_name);
            TextView productName = (TextView) convertView.findViewById(R.id.the_take_product_name);

            TheTakeData.TheTakeProduct product = getItem(position);
            if (product != null){
                if (productThumbnail != null){
                    PicassoTrustAll.loadImageIntoView(getActivity(), product.getThumbnailUrl(), productThumbnail);
                }
                if (brandName != null)
                    brandName.setText(product.productBrand);
                if (productName != null)
                    productName.setText(product.productName);
            }



            return convertView;
        }

        public int getCount() {
            if (selectedCategory != null && selectedCategory.products != null)
                return selectedCategory.products.size();
            else
                return 0;
        }

        public TheTakeData.TheTakeProduct getItem(int position) {
            if (selectedCategory != null && selectedCategory.products != null && position < selectedCategory.products.size())
                return selectedCategory.products.get(position);
            else
                return null;
        }

        public long getItemId(int position) {
            if (selectedCategory != null && selectedCategory.products != null && position < selectedCategory.products.size())
                return selectedCategory.products.get(position).productId;
            else
                return 0;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            TheTakeProductDetailFragment fragment = new TheTakeProductDetailFragment();
            TheTakeData.TheTakeProduct product = getItem(position);
            List<TheTakeData.TheTakeProduct> list = new ArrayList<TheTakeData.TheTakeProduct>();
            list.add(product);
            fragment.setProductList(list);
            if (getActivity() instanceof NextGenFragmentTransactionInterface){
                ((NextGenFragmentTransactionInterface)getActivity()).transitMainFragment(fragment);
            }
        }

    }
}
