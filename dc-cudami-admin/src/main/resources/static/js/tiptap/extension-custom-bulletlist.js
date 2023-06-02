import { Node, mergeAttributes, Mark, getMarkAttributes, wrappingInputRule } from '@tiptap/core';

// same as BulletList extension: just name and class name changed to match JSON "bullet_list" instead "bulletList" and "list_item" instead "listItem"
// and removed sourcemap comment at bottom

const ListItem = Node.create({
    name: 'list_item',
    addOptions() {
        return {
            HTMLAttributes: {},
        };
    },
    content: 'paragraph block*',
    defining: true,
    parseHTML() {
        return [
            {
                tag: 'li',
            },
        ];
    },
    renderHTML({ HTMLAttributes }) {
        return ['li', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
    },
    addKeyboardShortcuts() {
        return {
            Enter: () => this.editor.commands.splitListItem(this.name),
            Tab: () => this.editor.commands.sinkListItem(this.name),
            'Shift-Tab': () => this.editor.commands.liftListItem(this.name),
        };
    },
});

const TextStyle = Mark.create({
    name: 'textStyle',
    addOptions() {
        return {
            HTMLAttributes: {},
        };
    },
    parseHTML() {
        return [
            {
                tag: 'span',
                getAttrs: element => {
                    const hasStyles = element.hasAttribute('style');
                    if (!hasStyles) {
                        return false;
                    }
                    return {};
                },
            },
        ];
    },
    renderHTML({ HTMLAttributes }) {
        return ['span', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
    },
    addCommands() {
        return {
            removeEmptyTextStyle: () => ({ state, commands }) => {
                const attributes = getMarkAttributes(state, this.type);
                const hasStyles = Object.entries(attributes).some(([, value]) => !!value);
                if (hasStyles) {
                    return true;
                }
                return commands.unsetMark(this.name);
            },
        };
    },
});

const inputRegex = /^\s*([-+*])\s$/;
const CustomBulletList = Node.create({
    name: 'bullet_list',
    addOptions() {
        return {
            itemTypeName: 'list_item',
            HTMLAttributes: {},
            keepMarks: false,
            keepAttributes: false,
        };
    },
    group: 'block list',
    content() {
        return `${this.options.itemTypeName}+`;
    },
    parseHTML() {
        return [
            { tag: 'ul' },
        ];
    },
    renderHTML({ HTMLAttributes }) {
        return ['ul', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
    },
    addCommands() {
        return {
            toggleBulletList: () => ({ commands, chain }) => {
                if (this.options.keepAttributes) {
                    return chain().toggleList(this.name, this.options.itemTypeName, this.options.keepMarks).updateAttributes(ListItem.name, this.editor.getAttributes(TextStyle.name)).run();
                }
                return commands.toggleList(this.name, this.options.itemTypeName, this.options.keepMarks);
            },
        };
    },
    addKeyboardShortcuts() {
        return {
            'Mod-Shift-8': () => this.editor.commands.toggleBulletList(),
        };
    },
    addInputRules() {
        let inputRule = wrappingInputRule({
            find: inputRegex,
            type: this.type,
        });
        if (this.options.keepMarks || this.options.keepAttributes) {
            inputRule = wrappingInputRule({
                find: inputRegex,
                type: this.type,
                keepMarks: this.options.keepMarks,
                keepAttributes: this.options.keepAttributes,
                getAttributes: () => { return this.editor.getAttributes(TextStyle.name); },
                editor: this.editor,
            });
        }
        return [
            inputRule,
        ];
    },
});

export { CustomBulletList, CustomBulletList as default, inputRegex };
