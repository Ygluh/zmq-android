import zmq
import os
from datetime import datetime

class ZMQServer:
    def __init__(self, port=2223):
        self.context = zmq.Context()
        self.socket = self.context.socket(zmq.REP)
        self.socket.bind(f"tcp://*:{port}")
        self.packet_count = 0
        self.data_file = "received_data.txt"
        print(f"[SERVER] Server started on port {port}")
        
    def save_to_file(self, data):
        """Сохраняет полученные данные в файл"""
        with open(self.data_file, "a", encoding="utf-8") as f:
            timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            f.write(f"[{timestamp}] Packet #{self.packet_count}: {data}\n")
    
    def display_saved_data(self):
        """Выводит все сохраненные данные на экран"""
        print("\n" + "="*50)
        print("[SERVER] All saved data:")
        print("="*50)
        if os.path.exists(self.data_file):
            with open(self.data_file, "r", encoding="utf-8") as f:
                content = f.read()
                print(content)
        else:
            print("No data received yet.")
        print("="*50 + "\n")
    
    def run(self):
        """Основной цикл сервера"""
        try:
            while True:
                # Получаем сообщение от клиента
                message = self.socket.recv_string()
                self.packet_count += 1
                
                print(f"[SERVER] Received #{self.packet_count}: {message}")
                
                # Сохраняем в файл
                self.save_to_file(message)
                
                # Отправляем ответ
                response = "Hello from Server!"
                self.socket.send_string(response)
                print(f"[SERVER] Sent: {response}")
                
        except KeyboardInterrupt:
            print("\n[SERVER] Shutting down...")
            self.display_saved_data()
            print(f"[SERVER] Total packets received: {self.packet_count}")
        finally:
            self.socket.close()
            self.context.term()

if __name__ == "__main__":
    server = ZMQServer()
    server.run()