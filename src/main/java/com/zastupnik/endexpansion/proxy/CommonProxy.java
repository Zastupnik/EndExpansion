package com.zastupnik.endexpansion.proxy;

public class CommonProxy {
    // Этот метод вызывается на сервере (пустой) и на клиенте (через ClientProxy)
    public void registerRenderers() {
        // На сервере ничего не рендерим
    }

    public void registerTileEntities() {
        // Регистрация логики блоков (нужна и серверу, и клиенту)
    }
}