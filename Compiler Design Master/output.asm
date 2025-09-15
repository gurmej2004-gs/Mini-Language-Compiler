section .data
  x_msg db 'x =', 0
  x_msg_len equ $ - x_msg
  y_msg db 'y =', 0
  y_msg_len equ $ - y_msg
  newline db 0xA, 0
  newline_len equ $ - newline
  num_buf times 20 db 0
section .text
  global _main
_main:
  push rbp
  mov rbp, rsp
  sub rsp, 16
  mov rax, 8
  mov [rbp - 8], rax
  mov rax, 6
  mov [rbp - 0], rax
  mov rax, [rbp - 8]
  lea rsi, [rel num_buf]
  call int_to_string
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel x_msg]
  mov rdx, x_msg_len
  syscall
  lea rsi, [rel num_buf]
  mov rdx, rcx
  syscall
  lea rsi, [rel newline]
  mov rdx, newline_len
  syscall
  mov rax, [rbp]
  lea rsi, [rel num_buf]
  call int_to_string
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel y_msg]
  mov rdx, y_msg_len
  syscall
  lea rsi, [rel num_buf]
  mov rdx, rcx
  syscall
  lea rsi, [rel newline]
  mov rdx, newline_len
  syscall
  mov rax, 0x2000001
  xor rdi, rdi
  syscall
  leave
  ret
int_to_string:
  push rbx
  push rcx
  push rax
  mov rbx, 10
  xor rcx, rcx
  mov rdi, rsi
  push rdi
  test rax, rax
  jns .non_negative
  neg rax
.non_negative:
  or rax, rax
  jz .handle_zero
.convert_loop:
  xor rdx, rdx
  div rbx
  add dl, '0'
  mov [rdi], dl
  inc rdi
  inc rcx
  test rax, rax
  jnz .convert_loop
  jmp .done
.handle_zero:
  mov byte [rdi], '0'
  inc rdi
  mov rcx, 1
.done:
  mov byte [rdi], 0
  pop rdi
  pop rax
  pop rcx
  pop rbx
  ret
