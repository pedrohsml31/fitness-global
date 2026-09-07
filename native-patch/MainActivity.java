package com.pedro.fitnessglobal;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

/**
 * Os plugins proprios do app precisam ser registrados aqui, um por um — a Capacitor so
 * descobre sozinha os que vem de node_modules. Esquecer uma linha faz o plugin
 * simplesmente nao existir no JS, sem erro nenhum.
 *
 * O registro roda ANTES do super.onCreate de proposito: o UpdaterPlugin desfaz no load()
 * uma atualizacao que nao abriu, e isso precisa acontecer antes de a Bridge decidir de
 * qual pasta servir os arquivos.
 */
public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(HeadsetPlugin.class);
        registerPlugin(RestChannelPlugin.class);
        registerPlugin(UpdaterPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
