import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes } from '@angular/router'; // Necessário para a tipagem em app.config.ts

// --- DEFINIÇÃO DAS INTERFACES DE DADOS ---
interface Account {
  contaId: string;
  agencia: string;
  numeroConta: string;
  saldo: number;
  nomeCompleto: string;
  cpf: string;
}

interface Transaction {
  id: string;
  tipo: 'DEPOSITO' | 'SAQUE' | 'TRANSFERENCIA';
  valor: number;
  data: string;
  descricao: string;
}

// --- CONFIGURAÇÃO DE URLS DE MICROSERVIÇOS ---
// ATENÇÃO: Verifique se estas portas correspondem aos seus backends rodando!
const SECURITY_API_URL = 'http://localhost:8081/auth'; 
const ACCOUNTS_API_URL = 'http://localhost:8082/contas';
const TRANSFERS_API_URL = 'http://localhost:8083/transferencias';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule], // Importa FormsModule para usar [(ngModel)]
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col items-center p-4 font-sans">
      <header class="w-full max-w-4xl text-center py-6">
        <h1 class="text-4xl font-extrabold text-blue-800 tracking-tight">Ceará Bank</h1>
        <p class="text-sm text-gray-500 mt-1">O seu banco digital cearense.</p>
      </header>

      <!-- SEÇÃO DO STATUS DA API E ERROS -->
      <div *ngIf="apiStatus() || apiError()" 
           class="w-full max-w-md p-3 rounded-lg text-center font-medium shadow-md transition-all duration-300"
           [ngClass]="{'bg-red-100 border-red-400 text-red-700 border': apiError(), 'bg-green-100 border-green-400 text-green-700 border': !apiError()}">
        <span *ngIf="apiStatus()">{{ apiStatus() }}</span>
        <span *ngIf="apiError()">Erro: {{ apiError() }}</span>
      </div>

      <!-- MAIN CONTAINER -->
      <main class="w-full max-w-4xl mt-6 p-6 bg-white rounded-xl shadow-2xl">
        
        <!-- RENDERIZAÇÃO CONDICIONAL BASEADA NO ESTADO DE AUTENTICAÇÃO -->

        <!-- 1. TELA DE LOGIN/CADASTRO -->
        <ng-container *ngIf="authStatus() === 'logged_out'">
          <div class="flex justify-center mb-6">
            <button (click)="viewState.set('login')" [class.bg-blue-600]="viewState() === 'login'" [class.bg-gray-200]="viewState() !== 'login'"
                    class="px-6 py-2 rounded-l-lg font-semibold text-white transition-colors duration-200">
              Login
            </button>
            <button (click)="viewState.set('register')" [class.bg-blue-600]="viewState() === 'register'" [class.bg-gray-200]="viewState() !== 'register'"
                    class="px-6 py-2 rounded-r-lg font-semibold text-white transition-colors duration-200">
              Criar Conta
            </button>
          </div>
          
          <div *ngIf="viewState() === 'login'">
            <h2 class="text-2xl font-bold mb-4 text-center text-gray-700">Acessar Minha Conta</h2>
            <div class="space-y-4 max-w-sm mx-auto">
              <input type="text" [(ngModel)]="loginData.cpf" placeholder="CPF (somente números)" required
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <input type="password" [(ngModel)]="loginData.senha" placeholder="Senha" required
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <button (click)="handleLogin()" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 rounded-lg transition duration-200 shadow-md">
                Entrar
              </button>
            </div>
          </div>

          <div *ngIf="viewState() === 'register'">
            <h2 class="text-2xl font-bold mb-4 text-center text-gray-700">Novo Cliente Ceará Bank</h2>
            <div class="space-y-4 max-w-sm mx-auto">
              <input type="text" [(ngModel)]="registerData.nome" placeholder="Nome Completo" required
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <input type="text" [(ngModel)]="registerData.cpf" placeholder="CPF (somente números)" required
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <input type="password" [(ngModel)]="registerData.senha" placeholder="Criar Senha" required
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <button (click)="handleRegister()" class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-lg transition duration-200 shadow-md">
                Criar Conta
              </button>
            </div>
          </div>
        </ng-container>

        <!-- 2. TELA PRINCIPAL (LOGADO) -->
        <ng-container *ngIf="authStatus() === 'logged_in'">
          <div class="flex justify-between items-center mb-6 border-b pb-4">
            <h2 class="text-3xl font-extrabold text-blue-800">
              Olá, {{ userName() }}!
            </h2>
            <button (click)="handleLogout()" class="text-sm text-red-600 hover:text-red-800 font-medium">
              Sair
            </button>
          </div>

          <!-- Navegação Interna -->
          <div class="flex space-x-4 mb-8">
            <button *ngFor="let tab of activeTabs" (click)="activeTab.set(tab)"
                    class="px-4 py-2 rounded-full font-semibold transition-colors duration-200"
                    [ngClass]="{'bg-blue-600 text-white shadow-lg': activeTab() === tab, 'bg-gray-100 text-gray-700 hover:bg-blue-100': activeTab() !== tab}">
              {{ tab }}
            </button>
          </div>

          <!-- Saldo e Conta -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8 p-6 bg-blue-50 rounded-lg border border-blue-200 shadow-inner">
            <div class="md:col-span-1">
              <p class="text-sm font-semibold text-gray-600">Agência / Conta</p>
              <p class="text-lg font-mono text-blue-800">{{ accountInfo()?.agencia || '---' }} / {{ accountInfo()?.numeroConta || '---' }}</p>
            </div>
            <div class="md:col-span-2">
              <p class="text-sm font-semibold text-gray-600">Saldo Disponível</p>
              <p class="text-4xl font-bold text-green-600">R$ {{ accountInfo()?.saldo | number: '1.2-2' }}</p>
            </div>
          </div>

          <!-- CONTEÚDO DA ABA ATIVA -->

          <!-- Aba 1: EXTRATO -->
          <ng-container *ngIf="activeTab() === 'Extrato'">
            <h3 class="text-2xl font-bold text-gray-700 mb-4">Últimas Transações</h3>
            <div class="space-y-2">
              <div *ngIf="transactions().length === 0" class="p-4 bg-gray-100 rounded-lg text-center text-gray-500">
                Nenhuma transação encontrada.
              </div>
              <div *ngFor="let tx of transactions()" class="flex justify-between items-center p-3 border-b border-gray-100 hover:bg-gray-50 transition duration-150 rounded-md">
                <div class="flex-1">
                  <p class="font-semibold" [ngClass]="{'text-red-500': tx.tipo !== 'DEPOSITO', 'text-green-600': tx.tipo === 'DEPOSITO'}">
                    {{ tx.tipo }}
                  </p>
                  <p class="text-xs text-gray-500">{{ tx.descricao }}</p>
                </div>
                <div class="text-right">
                  <p class="font-bold" [ngClass]="{'text-red-500': tx.tipo !== 'DEPOSITO', 'text-green-600': tx.tipo === 'DEPOSITO'}">
                    R$ {{ tx.valor | number: '1.2-2' }}
                  </p>
                  <p class="text-xs text-gray-400">{{ tx.data | slice:0:10 }}</p>
                </div>
              </div>
            </div>
          </ng-container>

          <!-- Aba 2: TRANSFERIR -->
          <ng-container *ngIf="activeTab() === 'Transferir'">
            <h3 class="text-2xl font-bold text-gray-700 mb-4">Realizar Transferência (PIX)</h3>
            <div class="space-y-4 max-w-lg mx-auto p-6 border rounded-xl shadow-lg">
              <input type="text" [(ngModel)]="transferData.cpfDestino" placeholder="CPF Destino (somente números)"
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <input type="number" [(ngModel)]="transferData.valor" placeholder="Valor (R$)"
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              <input type="text" [(ngModel)]="transferData.descricao" placeholder="Descrição (Opcional)"
                     class="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500">
              
              <button (click)="handleTransfer()" class="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 rounded-lg transition duration-200 shadow-md">
                Confirmar Transferência
              </button>
            </div>
          </ng-container>

        </ng-container>
      </main>
    </div>
  `,
})
export class App {
  // Array tipado usado no *ngFor. Corrigido o erro NG5.
  readonly activeTabs = ['Extrato', 'Transferir'] as const;

  // --- STATE MANAGEMENT (SIGNALS) ---
  authStatus = signal<'logged_out' | 'logged_in' | 'loading'>('logged_out');
  viewState = signal<'login' | 'register'>('login');
  activeTab = signal<'Extrato' | 'Transferir'>('Extrato');
  
  apiStatus = signal<string | null>(null);
  apiError = signal<string | null>(null);

  token = signal<string | null>(null);
  accountInfo = signal<Account | null>(null);
  transactions = signal<Transaction[]>([]);

  // --- FORM DATA ---
  loginData = { cpf: '', senha: '' };
  registerData = { nome: '', cpf: '', senha: '' };
  transferData = { cpfDestino: '', valor: 0, descricao: '' };

  // Computed Signal para exibir apenas o primeiro nome do cliente (RESOLVE O ERRO TS2532)
  userName = computed(() => {
    const info = this.accountInfo();
    if (info && info.nomeCompleto) {
      return info.nomeCompleto.split(' ')[0];
    }
    return 'Cliente';
  });

  constructor() {
    // Tenta carregar token salvo no storage ao iniciar
    if (localStorage.getItem('token')) {
      this.token.set(localStorage.getItem('token'));
      this.authStatus.set('logged_in');
      this.loadAccountData();
    }
  }

  // --- HELPER PARA CHAMADAS DE API ---
  private async apiCall(url: string, method: string, body?: any): Promise<any> {
    this.apiStatus.set('Processando...');
    this.apiError.set(null);

    const headers: HeadersInit = { 'Content-Type': 'application/json' };
    if (this.token()) {
      headers['Authorization'] = `Bearer ${this.token()}`;
    }

    try {
      const response = await fetch(url, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null,
      });

      if (response.ok) {
        this.apiStatus.set('Sucesso!');
        // Se a resposta for 204 No Content, retorna true
        if (response.status === 204) return true;
        
        // Tenta parsear JSON
        const text = await response.text();
        return text ? JSON.parse(text) : true;
      } else {
        const errorText = await response.text();
        throw new Error(errorText || `Erro na API: ${response.status} ${response.statusText}`);
      }
    } catch (e: any) {
      const errorMessage = e instanceof Error ? e.message : 'Erro de conexão ou servidor.';
      this.apiError.set(errorMessage);
      this.apiStatus.set(null);
      console.error('API Call Error:', e);

      if (errorMessage.includes('Unauthorized') || errorMessage.includes('401')) {
         this.handleLogout();
      }
      return null;
    } finally {
      // Limpa a mensagem de status após 3 segundos
      setTimeout(() => this.apiStatus.set(null), 3000);
    }
  }

  // --- LÓGICA DE NEGÓCIO ---

  async handleLogin() {
    this.authStatus.set('loading');
    
    const data = {
      cpf: this.loginData.cpf,
      senha: this.loginData.senha
    };

    const result = await this.apiCall(`${SECURITY_API_URL}/login`, 'POST', data);

    if (result && result.token) {
      this.token.set(result.token);
      localStorage.setItem('token', result.token);
      this.authStatus.set('logged_in');
      this.loginData = { cpf: '', senha: '' }; // Limpa o formulário
      this.loadAccountData();
    } else {
      this.authStatus.set('logged_out');
    }
  }

  async handleRegister() {
    this.authStatus.set('loading');

    // 1. CHAMA SEGURANÇA para criar o usuário e senha
    const user = {
      cpf: this.registerData.cpf,
      senha: this.registerData.senha,
      nome: this.registerData.nome
    };
    const tokenResult = await this.apiCall(`${SECURITY_API_URL}/register`, 'POST', user);

    if (tokenResult && tokenResult.token) {
      this.token.set(tokenResult.token);
      localStorage.setItem('token', tokenResult.token);
      
      // 2. CHAMA CONTAS para criar a conta bancária
      const accountData = {
        cpf: this.registerData.cpf,
        nomeCompleto: this.registerData.nome
      };
      
      const accountResult = await this.apiCall(ACCOUNTS_API_URL, 'POST', accountData);

      if (accountResult) {
        this.authStatus.set('logged_in');
        this.registerData = { nome: '', cpf: '', senha: '' }; // Limpa o formulário
        this.loadAccountData(); // Carrega os dados da nova conta
        this.apiStatus.set('Conta criada com sucesso e login efetuado!');
      } else {
         // Se a conta falhar, precisamos invalidar o token de segurança (melhoria futura)
         this.handleLogout();
      }

    } else {
      this.authStatus.set('logged_out');
    }
  }

  handleLogout() {
    this.token.set(null);
    this.accountInfo.set(null);
    this.transactions.set([]);
    localStorage.removeItem('token');
    this.authStatus.set('logged_out');
    this.viewState.set('login'); // Volta para a tela de login
  }

  async loadAccountData() {
    if (!this.token()) return;

    // Carrega dados da conta (ex: /contas/meu-cpf)
    const account: Account = await this.apiCall(`${ACCOUNTS_API_URL}/me`, 'GET');
    if (account) {
      this.accountInfo.set(account);
      this.loadTransactions(account.numeroConta);
    }
  }

  async loadTransactions(numeroConta: string) {
    if (!this.token()) return;

    // Carrega o extrato (ex: /contas/{numeroConta}/extrato)
    const txs: Transaction[] = await this.apiCall(`${ACCOUNTS_API_URL}/${numeroConta}/extrato`, 'GET');
    if (txs) {
      this.transactions.set(txs);
    }
  }

  async handleTransfer() {
    if (!this.token() || !this.accountInfo()) return;
    
    if (this.transferData.valor <= 0) {
      this.apiError.set('O valor deve ser maior que zero.');
      return;
    }
    
    // Constrói o objeto de transferência
    const transferPayload = {
      contaOrigem: this.accountInfo()!.numeroConta,
      agenciaOrigem: this.accountInfo()!.agencia,
      cpfDestino: this.transferData.cpfDestino,
      valor: this.transferData.valor,
      descricao: this.transferData.descricao
    };

    // CHAMA TRANSFERENCIAS
    const success = await this.apiCall(`${TRANSFERS_API_URL}/transferir`, 'POST', transferPayload);

    if (success) {
      this.apiStatus.set('Transferência enviada com sucesso! Atualizando saldo...');
      this.transferData = { cpfDestino: '', valor: 0, descricao: '' }; // Limpa o formulário
      this.loadAccountData(); // Recarrega saldo e extrato
    }
  }
}