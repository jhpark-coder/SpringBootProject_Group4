import React, { useState, useEffect, useRef } from 'react';
import { io } from 'socket.io-client';
import UserList from './UserList';
import ChatRoom from './ChatRoom';
import ChatStats from './ChatStats';
import './ChatDashboard.css';

const ChatDashboard = () => {
    const [socket, setSocket] = useState(null); // 소켓 연결 상태
    const [users, setUsers] = useState(new Map()); // 사용자 목록
    const [currentUser, setCurrentUser] = useState(null); // 현재 선택된 사용자
    const [messages, setMessages] = useState([]); // 현재 선택된 사용자의 메시지 목록
    const [allMessages, setAllMessages] = useState(new Map()); // 모든 사용자의 메시지 저장
    const [unreadCounts, setUnreadCounts] = useState(new Map()); // 각 사용자별 안읽은 메시지 수
    const [connectionStatus, setConnectionStatus] = useState('연결 중...');
    const currentUserRef = useRef(currentUser); // 현재 선택된 사용자 참조

    useEffect(() => {
        const serverUrl = import.meta.env.VITE_NOTIFICATION_SERVER_URL || 'http://localhost:3000';
        // Socket.IO 연결 (관리자 권한으로)
        const newSocket = io(serverUrl, {
            transports: ['websocket', 'polling'],
            auth: {
                userId: 1, // 관리자 ID
                roles: ['ROLE_ADMIN'] // 관리자 역할
            }
        });

        newSocket.on('connect', () => {
            console.log('✅ 관리자 대시보드 연결 성공');
            setConnectionStatus('연결됨');
            joinAsAdmin(newSocket);
        });

        newSocket.on('disconnect', () => {
            console.log('🔗 관리자 대시보드 연결 해제');
            setConnectionStatus('연결 해제됨');
        });

        newSocket.on('connect_error', (error) => {
            console.error('❌ 관리자 대시보드 연결 오류:', error);
            setConnectionStatus('연결 오류');
        });

        // 사용자 관련 이벤트
        newSocket.on('userJoined', (data) => {
            addUser(data.sender);
        });

        newSocket.on('userDisconnected', (data) => {
            removeUser(data.sender);
        });

        // 메시지 관련 이벤트
        newSocket.on('userMessage', (data) => {
            // 사용자 목록에 최근 메시지 정보 업데이트
            setUsers(prev => {
                const newUsers = new Map(prev);
                const user = newUsers.get(data.sender);
                if (user) {
                    newUsers.set(data.sender, {
                        ...user,
                        lastMessage: {
                            content: data.content,
                            timestamp: data.timestamp
                        }
                    });
                }
                return newUsers;
            });

            // 안읽은 메시지 수 업데이트 (현재 선택된 사용자가 아닌 경우에만)
            if (currentUserRef.current !== data.sender) {
                setUnreadCounts(prev => {
                    const newCounts = new Map(prev);
                    const currentCount = newCounts.get(data.sender) || 0;
                    newCounts.set(data.sender, currentCount + 1);
                    return newCounts;
                });

                // 알림 표시
                showNotification(data.sender, data.content);
            } else {
                // 현재 선택된 사용자의 메시지라면 채팅방에 표시
                handleUserMessage(data);
            }
        });

        // 관리자 응답 수신 (관리자가 보낸 메시지)
        newSocket.on('adminReply', (data) => {
            if (currentUserRef.current === data.recipient) {
                handleUserMessage(data);
            }
        });

        // 채팅 내역 수신
        newSocket.on('chatHistory', (data) => {
            console.log('📨 채팅 내역 수신:', data);
            console.log('🔍 현재 선택된 사용자:', currentUserRef.current);
            if (data.userId === currentUserRef.current) {
                console.log('✅ 현재 사용자와 일치하는 채팅 내역:', data.userId);
                const sorted = (data.history || []).sort((a, b) =>
                    new Date(a.timestamp) - new Date(b.timestamp)
                );
                console.log('📋 정렬된 채팅 내역:', sorted);
                setMessages(sorted);

                // DB에서 받은 데이터로 allMessages 업데이트 (중복 방지)
                setAllMessages(prev => {
                    const newMessages = new Map(prev);
                    newMessages.set(data.userId, sorted);
                    return newMessages;
                });
            } else {
                console.log('❌ 현재 사용자와 일치하지 않는 채팅 내역:', {
                    receivedUserId: data.userId,
                    currentUser: currentUserRef.current
                });
            }
        });

        setSocket(newSocket);

        // 알림 권한 요청
        if (Notification.permission === 'default') {
            Notification.requestPermission();
        }

        return () => {
            newSocket.disconnect();
        };
    }, []); // 소켓 연결은 한 번만

    // currentUser가 변경될 때마다 ref 업데이트
    useEffect(() => {
        currentUserRef.current = currentUser;
    }, [currentUser]);

    const joinAsAdmin = (socket) => {
        socket.emit('joinAsAdmin', {
            sender: '관리자',
            type: 'JOIN'
        });
    };

    const normalizeUsername = (username) => {
        return username.startsWith('사용자_') ? username : `사용자_${username}`;
    };

    const addUser = (username) => {
        const fullUsername = normalizeUsername(username);
        setUsers(prev => {
            const newUsers = new Map(prev);
            if (!newUsers.has(fullUsername)) {
                newUsers.set(fullUsername, {
                    username: fullUsername,
                    status: 'online',
                    lastMessage: null
                });
            }
            return newUsers;
        });
    };

    const removeUser = (username) => {
        setUsers(prev => {
            const newUsers = new Map(prev);
            const user = newUsers.get(username);
            if (user) {
                // 사용자를 삭제하지 않고 상태만 offline으로 변경
                newUsers.set(username, {
                    ...user,
                    status: 'offline'
                });
            }
            return newUsers;
        });

        // 현재 선택된 사용자가 오프라인이 되어도 채팅방은 유지
        // 채팅 내역은 DB에 저장되어 있으므로 계속 볼 수 있음
    };

    const handleUserMessage = (data) => {
        setMessages(prev => [...prev, data]);
    };

    const selectUser = (username) => {
        const fullUsername = normalizeUsername(username);
        console.log('👤 사용자 선택:', { original: username, normalized: fullUsername });
        setCurrentUser(fullUsername);

        // 채팅방 진입 시 안읽은 메시지 수 리셋 (읽음 처리)
        setUnreadCounts(prev => {
            const newCounts = new Map(prev);
            newCounts.set(fullUsername, 0);
            return newCounts;
        });

        // 항상 DB에서 최신 데이터를 가져오도록 수정
        setMessages([]); // 로딩 상태 표시
        if (fullUsername && socket) {
            console.log('📤 채팅 내역 요청 전송:', { userId: fullUsername, socketConnected: socket.connected });
            socket.emit('getHistory', { userId: fullUsername });
        } else {
            console.warn('⚠️ 채팅 내역 요청 실패:', {
                fullUsername,
                socketExists: !!socket,
                socketConnected: socket?.connected
            });
        }
    };

    const backToUserList = () => {
        setCurrentUser(null);
        setMessages([]);
    };

    // 안읽은 채팅방 수 계산 (상담 대기 중인 고객 수)
    const calculateUnreadChatRooms = () => {
        let count = 0;
        unreadCounts.forEach((unreadCount) => {
            if (unreadCount > 0) {
                count++;
            }
        });
        return count;
    };

    const sendMessage = (content) => {
        if (!currentUser || !socket) return;

        const messageData = {
            content,
            sender: '관리자',
            recipient: currentUser,
            type: 'CHAT',
            timestamp: new Date().toISOString()
        };

        socket.emit('sendMessage', messageData);
        // 메시지는 서버에서 DB 저장 후 다시 받아서 표시되므로 여기서는 추가하지 않음
    };

    const showNotification = (sender, content) => {
        if (Notification.permission === 'granted') {
            const notification = new Notification(`새 메시지: ${sender}`, {
                body: content,
                icon: '/favicon.ico',
                requireInteraction: false, // 자동으로 사라지도록 설정
                silent: true // 소리 없이
            });

            // 1.5초 후 자동으로 알림 닫기
            setTimeout(() => {
                notification.close();
            }, 1500);
        }

        // 탭 제목 변경 (1초로 단축)
        const originalTitle = document.title;
        document.title = `[새 메시지] ${originalTitle}`;
        setTimeout(() => {
            document.title = originalTitle;
        }, 1000);
    };

    return (
        <div className="chat-dashboard">
            {/* 헤더 */}
            <div className="chat-header">
                <div>
                    <h4 className="mb-0">
                        <i className="fas fa-comments"></i>
                        관리자 채팅 대시보드
                    </h4>
                    <small className={`text-${connectionStatus === '연결됨' ? 'success' : 'danger'}`}>
                        {connectionStatus}
                    </small>
                </div>
                <ChatStats
                    onlineUsers={Array.from(users.values()).filter(user => user.status === 'online').length}
                    totalMessages={calculateUnreadChatRooms()}
                />
            </div>

            {/* 메인 영역 */}
            <div className="chat-main">
                {/* 사용자 목록 */}
                <UserList
                    users={Array.from(users.values())}
                    currentUser={currentUser}
                    onSelectUser={selectUser}
                    unreadCounts={unreadCounts}
                />

                {/* 채팅 영역 */}
                <ChatRoom
                    currentUser={currentUser}
                    messages={messages}
                    onSendMessage={sendMessage}
                    onBackToUserList={backToUserList}
                />
            </div>
        </div>
    );
};

export default ChatDashboard; 