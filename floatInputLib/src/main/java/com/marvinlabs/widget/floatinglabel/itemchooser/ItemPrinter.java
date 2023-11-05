package com.marvinlabs.widget.floatinglabel.itemchooser;


public interface ItemPrinter<ItemT> {

    public String print(ItemT item);


    public static class ToStringItemPrinter<ItemT> implements ItemPrinter<ItemT> {
        public String print(ItemT item) {
            return item==null ? "" : item.toString();
        }
    }
}
