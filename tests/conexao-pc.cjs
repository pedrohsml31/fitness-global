"use strict";
const assert=require('node:assert/strict');
const fs=require('node:fs');const path=require('node:path');const vm=require('node:vm');
const {webcrypto}=require('node:crypto');
const html=fs.readFileSync(path.join(__dirname,'..','index.html'),'utf8');
for(const m of html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)){if(m[1].trim())new vm.Script(m[1]);}
const trecho=html.slice(html.indexOf('async function criarEnvioPC'),html.indexOf('let envioCifradoPC'));
const contexto=vm.createContext({crypto:webcrypto,TextEncoder,btoa:s=>Buffer.from(s,'binary').toString('base64')});
const criar=vm.runInContext(trecho+'\ncriarEnvioPC',contexto);
(async()=>{
 const par=await webcrypto.subtle.generateKey({name:'RSA-OAEP',modulusLength:4096,publicExponent:new Uint8Array([1,0,1]),hash:'SHA-256'},true,['encrypt','decrypt']);
 const jwk=await webcrypto.subtle.exportKey('jwk',par.publicKey);
 const c={formato:1,kid:'a'.repeat(32),expiraUtc:new Date(Date.now()+3600000).toISOString(),jwk};
 const codigo='codigo-ficticio-para-cofre';const cfg={projectId:'projeto-ficticio',apiKey:'chave-publica-ficticia',ignorar:'nao-transferir-ficticio'};
 const envio=await criar(codigo,cfg,c);
 assert.match(envio,/^FGPC1\.[a-f0-9]{32}\.[A-Za-z0-9_-]+$/);assert(envio.length<1000);assert(!envio.includes(codigo));
 const bruto=Buffer.from(envio.split('.')[2],'base64url');
 const dados=JSON.parse(new TextDecoder().decode(await webcrypto.subtle.decrypt({name:'RSA-OAEP'},par.privateKey,bruto)));
 assert.equal(dados.code,codigo);assert.equal(dados.kid,c.kid);assert.deepEqual(dados.config,{projectId:cfg.projectId,apiKey:cfg.apiKey});
 await assert.rejects(criar(codigo,cfg,{...c,expiraUtc:new Date(0).toISOString()}));
 await assert.rejects(criar(codigo,cfg,{...c,jwk:{...jwk,d:'privada-ficticia'}}));
 await assert.rejects(criar('x'.repeat(1000),cfg,c));
 bruto[0]^=1;await assert.rejects(webcrypto.subtle.decrypt({name:'RSA-OAEP'},par.privateKey,bruto));
 console.log('OK: script completo válido; RSA-OAEP real cifra/decifra, texto curto sem código legível, somente configuração pública, expiração e adulteração recusadas.');
})().catch(()=>{console.error('Falhou conexão PC com dados fictícios.');process.exitCode=1;});
