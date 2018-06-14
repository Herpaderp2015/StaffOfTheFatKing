package com.sotfk;

import com.badlogic.gdx.files.FileHandle;

public class Store {
    public enum Currency { kMoney, kFood, kCrystals };
    private enum ItemType { kStandardItem, kBuff, kAbility, kMisc };

    private Player[] players_;
    private Currency currency_;

    public Store(Player[] players, FileHandle storeInfo) {
        this.players_ = players;
    }

    public void buyAbility(int i, int abiId, int price) {
        players_[i].addAbility(abiId);
        removeFunds(price);
    }

    private void removeFunds(int price) {
        switch (currency_) {
            case kFood:
            {
                Player.food -= price;
            }
            break;

            case kMoney:
            {
                Player.money -= price;
            }
            break;

            case kCrystals:
            break;

            default:
            break;
        }
    }

    private class StoreItem
    {
        public int price_;
        public ItemType itemType_;
        public int id_;

        private StoreItem (ItemType itemType, int id, int price) {
            price_ = price;
            itemType_ = itemType;
            id_ = id;
        }
    }
}
