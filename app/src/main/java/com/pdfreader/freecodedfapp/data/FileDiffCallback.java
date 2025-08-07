package com.pdfreader.freecodedfapp.data;

import androidx.recyclerview.widget.DiffUtil;

import com.pdfreader.freecodedfapp.models.PdfModel;

import java.util.List;

public class FileDiffCallback extends DiffUtil.Callback {
    private final List<PdfModel> newPDFModelList;
    private final List<PdfModel> oldPDFModelList;

    public FileDiffCallback(List<PdfModel> list, List<PdfModel> list2) {
        this.oldPDFModelList = list;
        this.newPDFModelList = list2;
    }

    public int getOldListSize() {
        return this.oldPDFModelList.size();
    }

    public int getNewListSize() {
        return this.newPDFModelList.size();
    }

    public boolean areItemsTheSame(int i, int i2) {
        return this.oldPDFModelList.get(i).getAbsolutePath().equals(this.newPDFModelList.get(i2).getAbsolutePath());
    }

    public boolean areContentsTheSame(int i, int i2) {
        return this.oldPDFModelList.get(i).equals(this.newPDFModelList.get(i2));
    }

    public Object getChangePayload(int i, int i2) {
        return super.getChangePayload(i, i2);
    }
}
