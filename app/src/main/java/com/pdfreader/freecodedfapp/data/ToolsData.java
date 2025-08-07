package com.pdfreader.freecodedfapp.data;

import android.content.Context;

import com.pdfreader.freecodedfapp.R;
import com.pdfreader.freecodedfapp.models.ToolModel;

import java.util.ArrayList;
import java.util.List;

public class ToolsData {
    public static List<ToolModel> getTools(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ToolModel(1, context.getString(R.string.split), "#9ccc66", R.drawable.ic_action_split));
        arrayList.add(new ToolModel(2, context.getString(R.string.extract_images), "#ffb74d", R.drawable.ic_action_extract_images));
        arrayList.add(new ToolModel(3, context.getString(R.string.save_as_pictures), "#7986cb", R.drawable.ic_action_save_photos));
        arrayList.add(new ToolModel(4, context.getString(R.string.edit_metadata), "#78909c", R.drawable.ic_action_edit_metadata));
        arrayList.add(new ToolModel(5, context.getString(R.string.compress), "#7ecdc8", R.drawable.ic_action_compress));
        arrayList.add(new ToolModel(6, context.getString(R.string.extract_text), "#9761a9", R.drawable.ic_action_extract_text));
        arrayList.add(new ToolModel(7, context.getString(R.string.images_to_pdf), "#f2af49", R.drawable.ic_action_image_to_pdf));
        arrayList.add(new ToolModel(8, context.getString(R.string.protect), "#7986cb", R.drawable.ic_action_protect));
        arrayList.add(new ToolModel(9, context.getString(R.string.unprotect), "#7ecdc8", R.drawable.ic_action_unprotect));
        arrayList.add(new ToolModel(10, context.getString(R.string.stamp), "#6db9e5", R.drawable.ic_action_stamp));
        arrayList.add(new ToolModel(11, context.getString(R.string.page_numbers), "#14b89a", R.drawable.ic_action_page_numbers));
        return arrayList;
    }
}
