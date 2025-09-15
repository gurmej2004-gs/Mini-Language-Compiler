section .data
  newline db 0xA, 0
  newline_len equ $ - newline
  num_buf times 20 db 0
section .text
  global _main
_main:
  push rbp
  mov rbp, rsp
  ; Assign x = 8
  mov rax, 8
  mov [rbp -8], rax
  ; Assign y = 6
  mov rax, 6
  mov [rbp -16], rax
  sub rsp, 16
  x_msg db 'x =', 0
  x_msg_len equ $ - x_msg
  y_msg db 'y =', 0
  y_msg_len equ $ - y_msg

  ; Print x = value
  mov rax, [rbp -8]
  lea rsi, [rel num_buf]
  call int_to_string
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel x_msg]
  mov rdx, x_msg_len
  syscall
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel num_buf]
  mov rdx, rcx
  syscall
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel newline]
  mov rdx, newline_len
  syscall

  ; Print y = value
  mov rax, [rbp -16]
  lea rsi, [rel num_buf]
  call int_to_string
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel y_msg]
  mov rdx, y_msg_len
  syscall
  mov rax, 0x2000004
  mov rdi, 1
  lea rsi, [rel num_buf]
  mov rdx, rcx
  syscall
  mov rax, 0x2000004
  mov rdi, 1
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
  push rdi
  push rsi
  mov rbp, rsi
  mov rbx, 10
  xor rcx, rcx
  mov byte [rbp], 0
  dec rbp
  cmp rax, 0
  jge .non_negative_int_to_string
  neg rax
  mov byte [rbp], '-'
  dec rbp
  inc rcx
.non_negative_int_to_string:
  cmp rax, 0
  jz .handle_zero_int_to_string
.convert_loop_int_to_string:
  xor rdx, rdx
  div rbx
  add dl, '0'
  mov [rbp], dl
  dec rbp
  inc rcx
  cmp rax, 0
  jnz .convert_loop_int_to_string
  jmp .done_int_to_string
.handle_zero_int_to_string:
  mov byte [rbp], '0'
  dec rbp
  mov rcx, 1
.done_int_to_string:
  inc rbp
  mov rsi, rbp
  pop rsi
  pop rdi
  pop rax
  pop rcx
  pop rbx
  ret
